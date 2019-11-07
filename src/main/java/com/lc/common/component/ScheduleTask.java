package com.lc.common.component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lc.common.config.TC5UConstant;
import com.lc.common.service.RedisService;
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
    private RedisService tc5uredisservice;

    @Value("${spring.profiles.active}")
    private String env;

    /**
     * MQ 消息失败检测定时任务
     */
    @Scheduled(fixedDelay = 30000)
    private void configureTasks() {
        String task_name = env + "_MQ_FAiL_CHECK_TASK";
        Boolean f = tc5uredisservice.hasKey(task_name, TC5UConstant.REDIS_DB_TASK);
        if (f) {
            return;
        }
        log.info("【执行定时任务】" + task_name);
        tc5uredisservice.hashPut(task_name, task_name, task_name, TC5UConstant.REDIS_DB_TASK);
        tc5uredisservice.expire(task_name, 30, TC5UConstant.REDIS_DB_TASK);
        // 取出所有消息
        Map<String, JSONObject> msgs = tc5uredisservice.hashFindAll(env + "MQMSG", TC5UConstant.REDIS_DB_OTHER);
        for (Map.Entry<String, JSONObject> msg : msgs.entrySet()) {
            String msg_id = msg.getKey();
            JSONObject obj = msg.getValue();
            Date send_date = obj.getDate("date");
            long send_second = send_date.getTime() + 1000 * 60;
            // 如果超过一分钟还没有投递至MQ 则表示投递失败
            if (System.currentTimeMillis() < send_second) {
                continue;
            } else {
                obj.put("date", new Date());
                tc5uredisservice.hashRemove(env + "MQMSG", msg_id, TC5UConstant.REDIS_DB_OTHER);
                rabbitMQSender.sendMsg(obj.getString("exchange"), obj.getString("routingKey"), JSON.toJSONString(obj.get("msg")));
            }
        }
    }
}
