package live.lumia.component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import live.lumia.utils.RedisUtil;
import live.lumia.utils.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;


/**
 * @author l5990
 */
@Slf4j
@ConditionalOnClass({RabbitTemplate.class, RedissonClient.class})
@Component
public class RabbitMqSend {

    @Autowired
    private RabbitTemplate rabbitTemplate;


    @Value("${spring.profiles.active:dev}")
    private String env;

    /**
     * 用于监听Server端给我们返回的确认请求,消息到了exchange，ack 就返回true
     */
    private final RabbitTemplate.ConfirmCallback confirmCallback = (correlationData, ack, cause) -> {
        if (ack && Objects.nonNull(correlationData)) {
            // 从缓存中删除已经投递成功的消息
            RedisUtil.hashRemove(env + "MQMSG", correlationData.getId());
            log.info("【消息投递成功】 " + correlationData.getId());
        } else {
            log.error("【消息投递失败】 ");
            if (correlationData == null) {
                return;
            }
            Message returnedMessage = correlationData.getReturnedMessage();
            if (Objects.isNull(returnedMessage)) {
                return;
            }
            MessageProperties messageProperties = returnedMessage.getMessageProperties();
            JSONObject rk = new JSONObject();
            rk.put("routingKey", messageProperties.getReceivedRoutingKey());
            rk.put("exchange", messageProperties.getExpiration());
            rk.put("msg", returnedMessage);
            rk.put("date", new Date());
            rk.put("num", messageProperties.getHeaders().getOrDefault("num", 0));
            String env = SpringUtil.getProperty("spring.profiles.active");
            RedisUtil.hashPut(env + "MQMSG", correlationData.getId(), rk);
        }
    };

    /**
     * 监听对不可达的消息进行后续处理;
     * 不可达消息：指定的路由key路由不到。
     */
    private final RabbitTemplate.ReturnCallback returnCallback = (message, replyCode, replyText, exchange, routingKey) -> {
        log.error("【消息投递失败】 {},{},{},{}", message, replyCode, exchange, routingKey);
    };


    @Async
    public void sendMsg(String exchange, String routingKey, String msg) {
        sendMsg(null, exchange, routingKey, msg, 0);
    }

    /**
     * 发送消息
     *
     * @param msgId
     * @param exchange
     * @param routingKey
     * @param msg
     * @param num
     */
    void sendMsg(String msgId, String exchange, String routingKey, String msg, int num) {
        rabbitTemplate.setConfirmCallback(confirmCallback);
        rabbitTemplate.setReturnCallback(returnCallback);
        if (StringUtils.isEmpty(msgId)) {
            msgId = UUID.randomUUID().toString();
        }
        Message message = MessageBuilder.withBody(msg.getBytes(StandardCharsets.UTF_8))
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .setContentEncoding(StandardCharsets.UTF_8.displayName())
                .setMessageId(msgId)
                .setHeader("num", num)
                .build();
        CorrelationData cd = new CorrelationData(msgId);
        rabbitTemplate.convertAndSend(exchange, routingKey, message, cd);
        log.info("【MQ投递消息】 exchange : {}, routingKey: {},  msg : {} , msgId: {}, num: {}", exchange, routingKey, JSON.toJSONString(msg), cd.getId(), num);
    }

}
