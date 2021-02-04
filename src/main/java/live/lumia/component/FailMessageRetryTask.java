package live.lumia.component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import live.lumia.annotations.RedisLock;
import live.lumia.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.CollectionUtils;

import java.util.Map;

/**
 * @author l5990
 */
@Slf4j
@ConditionalOnBean({RabbitTemplate.class, RedissonClient.class})
@EnableScheduling
@Configuration
public class FailMessageRetryTask {

    @Autowired
    private RabbitMqSend rabbitMqSend;


    @Value("${spring.profiles.active}")
    private String env;

    /**
     * MQ 消息失败检测定时任务
     */
    @RedisLock("MQ_FAiL_CHECK_TASK")
    @Async
    @Scheduled(cron = "0 0/2 * * * ?")
    public void configureTasks() {
        try {
            // 取出所有消息
            Map<String, JSONObject> msgs = RedisUtil.hashFindAll(env + "MQMSG");
            int maxTime = 3;
            if (CollectionUtils.isEmpty(msgs)) {
                log.debug("无失败的消息");
                return;
            }
            msgs.entrySet().parallelStream()
                    .forEach(msg -> {
                        String msgId = msg.getKey();
                        JSONObject obj = msg.getValue();
                        int num = obj.getInteger("num");
                        if (num < maxTime) {
                            rabbitMqSend.sendMsg(msgId, obj.getString("exchange"), obj.getString("routingKey"), JSON.toJSONString(obj.get("msg")), ++num);
                            RedisUtil.hashRemove(env + "MQMSG", msgId);
                        } else {
                            log.warn("消息投递失败{}次放弃投递{},消息ID：{}", maxTime, obj.toJSONString(), msgId);
                        }
                    });
            msgs.clear();
        } catch (Exception e) {
            log.error("【执行定时任务{}失败】", "MQ_FAiL_CHECK_TASK", e);
        }
    }
}
