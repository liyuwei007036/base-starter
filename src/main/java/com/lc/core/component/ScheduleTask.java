package com.lc.core.component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lc.core.enums.CommonConstant;
import com.lc.core.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author l5990
 */
@Slf4j
@ConditionalOnClass(RabbitAdmin.class)
@Component
@EnableScheduling
public class ScheduleTask {

    @Autowired
    private RabbitMqSend rabbitMqSend;

    @Autowired
    private RedisService redisService;

    @Value("${spring.profiles.active}")
    private String env;

    /**
     * MQ 消息失败检测定时任务
     */
    @Async
    @Scheduled(cron = "0 0/2 * * * ?")
    public void configureTasks() {
        String taskName = env + "_MQ_FAiL_CHECK_TASK".toUpperCase();
        boolean f = redisService.putIfAbsent(taskName, 1, CommonConstant.REDIS_DB_TASK, 60);
        if (!f) {
            log.info("【跳过执行定时任务】{}", taskName);
            return;
        }
        log.info("【执行定时任务】{}", taskName);
        try {
            // 取出所有消息
            Map<String, JSONObject> msgs = redisService.hashFindAll(env + "MQMSG", CommonConstant.REDIS_DB_OTHER);
            int maxTime = 3;
            msgs.entrySet().parallelStream().forEach(msg -> {
                String msgId = msg.getKey();
                JSONObject obj = msg.getValue();
                int num = obj.getInteger("num");
                if (num < maxTime) {
                    rabbitMqSend.sendMsg(msgId, obj.getString("exchange"), obj.getString("routingKey"), JSON.toJSONString(obj.get("msg")), ++num);
                    redisService.hashRemove(env + "MQMSG", msgId, CommonConstant.REDIS_DB_OTHER);
                } else {
                    log.error("消息投递失败3次放弃投递{}", obj.toJSONString());
                }
            });
            msgs.clear();
        } catch (Exception e) {
            log.error("【执行定时任务{}失败】", taskName, e);
        } finally {
            redisService.remove(taskName, CommonConstant.REDIS_DB_TASK);
        }
    }
}
