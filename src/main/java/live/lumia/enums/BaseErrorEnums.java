package live.lumia.enums;

import live.lumia.error.IErrorInterface;

/**
 * @author l5990
 */

public enum BaseErrorEnums implements IErrorInterface {
    /**
     * 错误提示
     */
    SUCCESS_CODE(1000, "SUCCESS"),
    BAD_REQUEST(1001, "错误的请求参数"),
    NOT_FOUND(1002, "错误的请求路径"),
    CONNECTION_ERROR(1003, "网络连接请求失败！"),
    METHOD_NOT_ALLOWED(1004, "不合法的请求方式"),
    DATABASE_ERROR(1005, "数据库异常"),
    BOUND_STATEMENT_NOT_FOUNT(1006, "找不到方法"),
    BAD_REQUEST_TYPE(1007, "错误的请求类型"),
    NO_PERMISSION(1008, "非法请求！"),
    NO_FILE_SELECT(1009, "未选择文件"),
    FILE_UPLOAD_SUCCESS(1010, "上传成功"),
    UPLOAD_FILE_TOO_LARGE(1011, "上传文件过大"),
    ERROR_AUTH(1012, "权限不足"),
    ERROR_SIGN(1013, "签名校验失败"),
    ERROR_SYS(1014, "服务器繁忙"),
    ERROR_CONFIG(1015, "系统配置错误"),
    ERROR_FILE_FORMAT(1016, "上传文件格式错误"),
    ERROR_ARGS(1017, "参数错误"),
    FILE_NOT_EXISTS(1018, "文件不存在"),
    OTHER_ERROR(1019, "其它错误"),
    REQUEST_FAIL(1020, "发起网络请求失败"),
    ARGS_FORMAT_ERROR(1021, "发起网络请求失败"),
    ERROR_LOGIN(1022, "请登录"),
    REQUEST_ARGS_ERROR(1023, "请求参数错误"),
    OPERATION_TOO_FAST(1024, "操作过快"),
    SYSTEM_ERROR(1025, "系统异常");

    private final int code;
    private final String msg;

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
