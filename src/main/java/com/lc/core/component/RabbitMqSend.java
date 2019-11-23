package com.lc.core.component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lc.core.enums.CommonConstant;
import com.lc.core.service.RedisService;
import com.lc.core.utils.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
@Component
public class RabbitMqSend {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RedisService<String, JSONObject> redisService;

    @Value("${spring.profiles.active:dev}")
    private String env;

    /**
     * 用于监听Server端给我们返回的确认请求,消息到了exchange，ack 就返回true
     */
    private final RabbitTemplate.ConfirmCallback confirmCallback = (correlationData, ack, cause) -> {
        if (ack && Objects.nonNull(correlationData)) {
            // 从缓存中删除已经投递成功的消息
            redisService.hashRemove(env + "MQMSG", correlationData.getId(), CommonConstant.REDIS_DB_OTHER);
            log.info("【消息投递成功】 " + correlationData.getId());
        } else {
            log.error("【消息投递失败】 ");
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
                .setContentEncoding("utf-8")
                .setMessageId(msgId)
                .build();
        CorrelationData cd = new CorrelationData(msgId);
        // 存放至redis 默认为未投递
        JSONObject rk = new JSONObject();
        rk.put("routingKey", routingKey);
        rk.put("exchange", exchange);
        rk.put("msg", msg);
        rk.put("date", new Date());
        rk.put("num", num);
        String env = SpringUtil.getProperty("spring.profiles.active");
        redisService.hashPut(env + "MQMSG", msgId, rk, CommonConstant.REDIS_DB_OTHER);
        rabbitTemplate.convertAndSend(exchange, routingKey, message, cd);
        log.info("【MQ投递消息】 exchange :" + exchange + " routingKey : " + routingKey + " msg : " + JSON.toJSONString(msg) + " msgId: " + cd.getId());
    }

}
