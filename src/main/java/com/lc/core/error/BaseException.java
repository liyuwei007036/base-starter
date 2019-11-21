package com.lc.core.error;

import lombok.Data;

/**
 * @author l5990
 */
@Data
public class BaseException extends RuntimeException {

    private int errCode;

    private String errMsg;

    private BaseException() {

    }

    public BaseException(IErrorInterface errorEnums) {
        super(errorEnums.getMsg());
        this.errCode = errorEnums.getCode();
        this.errMsg = errorEnums.getMsg();
    }

    public BaseException(IErrorInterface errorEnums, Exception e) {
        super(e);
        this.errCode = errorEnums.getCode();
        this.errMsg = errorEnums.getMsg();
    }
}

