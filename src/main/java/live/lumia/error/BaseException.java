package live.lumia.error;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author l5990
 */
@EqualsAndHashCode(callSuper = true)
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

