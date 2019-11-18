package com.lc.core.component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lc.core.config.CommonConstant;
import com.lc.core.service.RedisService;
import lombok.extern.log4j.Log4j2;
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
    private RabbitMQSender rabbitMQSender;

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
            return;
        }
        log.info("【执行定时任务】" + taskName);
        redisService.expire(taskName, 30, CommonConstant.REDIS_DB_TASK);
        // 取出所有消息
        Map<String, JSONObject> msgs = redisService.hashFindAll(env + "MQMSG", CommonConstant.REDIS_DB_OTHER);
        for (Map.Entry<String, JSONObject> msg : msgs.entrySet()) {
            String msgId = msg.getKey();
            JSONObject obj = msg.getValue();
            int num = obj.getInteger("num");
            if (num < 4) {
                redisService.hashRemove(env + "MQMSG", msgId, CommonConstant.REDIS_DB_OTHER);
                rabbitMQSender.sendMsg(msgId, obj.getString("exchange"), obj.getString("routingKey"), JSON.toJSONString(obj.get("msg")), ++num);
            }
        }
    }
}
