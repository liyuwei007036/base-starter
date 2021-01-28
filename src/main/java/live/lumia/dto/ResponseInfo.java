package live.lumia.dto;

import live.lumia.enums.BaseErrorEnums;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

/**
 * @author l5990
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseInfo<T> implements Serializable {

    private int code;

    private String msg;

    private T data;

    public ResponseInfo(BaseErrorEnums enums, T data) {
        this.code = enums.getCode();
        this.msg = enums.getMsg();
        this.data = data;
    }

    public ResponseInfo(BaseErrorEnums enums) {
        this.code = enums.getCode();
        this.msg = enums.getMsg();
    }

    public ResponseInfo(int code, String msg) {
        this.code = code;
        this.msg = msg;
        this.data = null;
    }

    public ResponseInfo() {
        this.code = BaseErrorEnums.SUCCESS_CODE.getCode();
        this.msg = BaseErrorEnums.SUCCESS_CODE.getMsg();
    }

    public ResponseInfo(T data) {
        this.code = BaseErrorEnums.SUCCESS_CODE.getCode();
        this.msg = BaseErrorEnums.SUCCESS_CODE.getMsg();
        this.data = data;
    }


}
