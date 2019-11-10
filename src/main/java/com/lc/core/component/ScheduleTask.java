package com.lc.core.component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lc.core.config.CommonConstant;
import com.lc.core.service.RedisService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

/**
 * @author l5990
 */
@Log4j2
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
    @Scheduled(fixedDelay = 30000)
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
            Date sendData = obj.getDate("date");
            long sendSecond = sendData.getTime() + 1000 * 60;
            // 如果超过一分钟还没有投递至MQ 则表示投递失败
            if (System.currentTimeMillis() >= sendSecond) {
                obj.put("date", new Date());
                redisService.hashRemove(env + "MQMSG", msgId, CommonConstant.REDIS_DB_OTHER);
                rabbitMQSender.sendMsg(obj.getString("exchange"), obj.getString("routingKey"), JSON.toJSONString(obj.get("msg")));
            }
        }
    }
}
