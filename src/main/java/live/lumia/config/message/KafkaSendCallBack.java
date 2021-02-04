package live.lumia.config.message;

import org.springframework.kafka.support.SendResult;

/**
 * @author lc
 * @date 2019-12-10
 */
public interface KafkaSendCallBack {

    /**
     * 发送前
     *
     * @param topic topic
     * @param msg   message
     */
    void before(String topic, String msg);

    /**
     * 发送成功
     *
     * @param stringStringSendResult res
     * @param msg                    message
     */
    void success(SendResult<String, String> stringStringSendResult, String msg);

    /**
     * 发送失败
     *
     * @param topic topic
     * @param msg   message
     */
    void fail(String topic, String msg);
}
