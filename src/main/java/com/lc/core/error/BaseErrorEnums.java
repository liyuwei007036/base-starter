package com.lc.core.error;

/**
 * @author l5990
 */

public enum BaseErrorEnums implements IErrorInterface {
    /**
     * 错误提示
     */
    SYSTEM_ERROR(-1, "系统异常"),
    BAD_REQUEST(-2, "错误的请求参数"),
    NOT_FOUND(-3, "找不到请求路径！"),
    CONNECTION_ERROR(-4, "网络连接请求失败！"),
    METHOD_NOT_ALLOWED(-5, "不合法的请求方式"),
    DATABASE_ERROR(-6, "数据库异常"),
    BOUND_STATEMENT_NOT_FOUNT(-7, "找不到方法！"),
    BAD_REQUEST_TYPE(-9, "错误的请求类型"),
    NO_PERMISSION(4, "非法请求！"),
    No_FileSELECT(21, "未选择文件"),
    FILEUPLOAD_SUCCESS(22, "上传成功"),
    FILEUPLOAD(52, "上传文件过大"),
    ERROR_AUTH(1401, "权限不足"),
    ERROR_SIGN(1403, "签名校验失败"),
    ERROR_SYS(1500, "服务器繁忙"),
    ERROR_CONFIG(1502, "系统配置错误"),
    ERROR_FILE_FORMAT(1200, "上传文件格式错误"),
    ERROR_ARGS(1503, "参数错误"),
    SUCCESS_CODE(0, "success"),
    FILE_NOT_EXISTS(-10, "文件不存在"),
    OTHER_ERROR(9999, "其它错误"),
    REQUEST_FAIL(1000, "发起网络请求失败");

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
