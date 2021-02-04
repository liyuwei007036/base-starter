package live.lumia.component;

import live.lumia.config.message.KafkaSendCallBack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

/**
 * @author lc
 * @date 2019-12-10
 */
@Slf4j
@ConditionalOnBean(KafkaTemplate.class)
@Component
public class KafkaSend {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Async
    public void send(String topic, String msg) {
        ListenableFuture<SendResult<String, String>> listenableFuture = kafkaTemplate.send(topic, msg);
        listenableFuture.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
            @Override
            public void onFailure(Throwable throwable) {
                log.error("【kafka 发送消息失败】topic :{}, msg:{} ", topic, msg, throwable);
            }

            @Override
            public void onSuccess(SendResult<String, String> stringStringSendResult) {
                log.info("【kafka 发送消息成功】topic :{}, msg:{} ", topic, msg);
            }
        });
    }


    @Async
    public void send(String topic, String msg, KafkaSendCallBack callBack) {
        callBack.before(topic, msg);
        ListenableFuture<SendResult<String, String>> listenableFuture = kafkaTemplate.send(topic, msg);
        listenableFuture.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
            @Override
            public void onFailure(Throwable throwable) {
                log.error("【kafka 发送消息失败】topic :{}, msg:{} ", topic, msg, throwable);
                callBack.fail(topic, msg);
            }

            @Override
            public void onSuccess(SendResult<String, String> stringStringSendResult) {
                log.info("【kafka 发送消息成功】topic :{}, msg:{} ", topic, msg);
                callBack.success(stringStringSendResult, msg);
            }
        });
    }
}
