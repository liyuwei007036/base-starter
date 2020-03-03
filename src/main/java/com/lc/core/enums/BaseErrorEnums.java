package com.lc.core.enums;

import com.lc.core.error.IErrorInterface;

/**
 * @author l5990
 */

public enum BaseErrorEnums implements IErrorInterface {
    /**
     * 错误提示
     */
    SUCCESS_CODE(1000, "success"),
    SYSTEM_ERROR(1000, "系统异常"),
    BAD_REQUEST(1001, "错误的请求参数"),
    NOT_FOUND(1002, "找不到请求路径！"),
    CONNECTION_ERROR(1003, "网络连接请求失败！"),
    METHOD_NOT_ALLOWED(1004, "不合法的请求方式"),
    DATABASE_ERROR(1005, "数据库异常"),
    BOUND_STATEMENT_NOT_FOUNT(1006, "找不到方法！"),
    BAD_REQUEST_TYPE(1007, "错误的请求类型"),
    NO_PERMISSION(1008, "非法请求！"),
    No_FileSELECT(1009, "未选择文件"),
    FILEUPLOAD_SUCCESS(1010, "上传成功"),
    FILEUPLOAD(1011, "上传文件过大"),
    ERROR_AUTH(1012, "权限不足"),
    ERROR_SIGN(1013, "签名校验失败"),
    ERROR_SYS(1014, "服务器繁忙"),
    ERROR_CONFIG(1015, "系统配置错误"),
    ERROR_FILE_FORMAT(1016, "上传文件格式错误"),
    ERROR_ARGS(1017, "参数错误"),
    FILE_NOT_EXISTS(1018, "文件不存在"),
    OTHER_ERROR(1019, "其它错误"),
    REQUEST_FAIL(1020, "发起网络请求失败"),
    ARGS_FORMAT_ERROR(1020, "发起网络请求失败"),
    REQUEST_ARGS_ERROR(1021, "发起网络请求失败");

    private int code;
    private String msg;

    BaseErrorEnums(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMsg() {
        return msg;
    }

}
