package com.lc.common.error;

/**
 * @author l5990
 */
public interface IErrorInterface {

    /**
     * 获取错误代码
     *
     * @return
     */
    int getCode();

    /**
     * 获取错误消息
     *
     * @return
     */
    String getMsg();
}
