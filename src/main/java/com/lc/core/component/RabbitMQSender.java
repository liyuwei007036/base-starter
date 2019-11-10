package com.lc.core.component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lc.core.config.CommonConstant;
import com.lc.core.utils.SpringUtil;
import com.lc.core.service.RedisService;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;


/**
 * @author l5990
 */
@Log4j2
@Component
public class RabbitMQSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RedisService<String, JSONObject> redisService;

    @Value("${spring.profiles.active}")
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

    };

    /**
     * 发送消息
     *
     * @param exchange   交换机名称
     * @param routingKey 路由键
     * @param msg        消息内容
     */
    public <T extends Serializable> void sendMsg(String exchange, String routingKey, T msg) {
        rabbitTemplate.setConfirmCallback(confirmCallback);
        rabbitTemplate.setReturnCallback(returnCallback);
        CorrelationData cd = new CorrelationData(UUID.randomUUID().toString());
        // 存放至redis 默认为未投递
        JSONObject rk = new JSONObject();
        rk.put("routingKey", routingKey);
        rk.put("exchange", exchange);
        rk.put("msg", msg);
        rk.put("date", new Date());
        String key = SpringUtil.getProperty("spring.profiles.active");
        redisService.hashPut(key + "MQMSG", cd.getId(), rk, CommonConstant.REDIS_DB_OTHER);
        rabbitTemplate.convertAndSend(exchange, routingKey, msg, cd);
        log.info("【MQ投递消息】 exchange :" + exchange + " routingKey : " + routingKey + " msg : " + JSON.toJSONString(msg) + " msgId: " + cd.getId());
    }

}
