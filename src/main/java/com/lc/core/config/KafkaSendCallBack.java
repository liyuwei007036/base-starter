package com.lc.core.config;

import org.springframework.kafka.support.SendResult;

/**
 * @author lc
 * @date 2019-12-10
 */
public interface KafkaSendCallBack {

    /**
     * 发送前
     *
     * @param topic
     * @param msg
     */
    void before(String topic, String msg);

    /**
     * 发送成功
     *
     * @param stringStringSendResult
     * @param msg
     */
    void success(SendResult<String, String> stringStringSendResult, String msg);

    /**
     * 发送失败
     *
     * @param topic
     * @param msg
     */
    void fail(String topic, String msg);
}
