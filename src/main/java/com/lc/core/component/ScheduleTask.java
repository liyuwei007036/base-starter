package com.lc.core.component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lc.core.enums.CommonConstant;
import com.lc.core.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author l5990
 */
@Slf4j
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
    @Scheduled(cron = "* */2 * * *?")
    private void configureTasks() {
        String taskName = env + "_MQ_FAiL_CHECK_TASK".toUpperCase();
        boolean f = redisService.hashPutIfAbsent(taskName, taskName, taskName, CommonConstant.REDIS_DB_TASK);
        if (!f) {
            log.info("【跳过执行定时任务】{}", taskName);
            return;
        }
        log.info("【执行定时任务】{}", taskName);
        try {
            redisService.expire(taskName, 30, CommonConstant.REDIS_DB_TASK);
            // 取出所有消息
            Map<String, JSONObject> msgs = redisService.hashFindAll(env + "MQMSG", CommonConstant.REDIS_DB_OTHER);
            int maxTime = 3;
            msgs.entrySet().parallelStream().forEach(msg -> {
                String msgId = msg.getKey();
                JSONObject obj = msg.getValue();
                int num = obj.getInteger("num");
                if (num < maxTime) {
                    redisService.hashRemove(env + "MQMSG", msgId, CommonConstant.REDIS_DB_OTHER);
                    rabbitMqSend.sendMsg(msgId, obj.getString("exchange"), obj.getString("routingKey"), JSON.toJSONString(obj.get("msg")), ++num);
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
