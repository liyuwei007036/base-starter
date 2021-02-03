package live.lumia.error;

import live.lumia.dto.ResponseInfo;
import live.lumia.enums.BaseErrorEnums;
import live.lumia.utils.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.bind.BindException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import java.net.ConnectException;
import java.sql.SQLException;
import java.util.List;

/**
 * 全局错误处理
 *
 * @author l5990
 */
@Slf4j
@RestControllerAdvice
public class DefaultExceptionHandler {
    /**
     * 文件上传错误异常的捕获
     *
     * @param e
     * @return
     */
    @ExceptionHandler(MultipartException.class)
    public ResponseInfo uploadFile(MultipartException e) {
        log.error("MultipartException ", e);
        return new ResponseInfo<>(BaseErrorEnums.UPLOAD_FILE_TOO_LARGE);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseInfo httpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.error("HttpMessageNotReadableException ", e);
        return new ResponseInfo<>(BaseErrorEnums.REQUEST_ARGS_ERROR);
    }

    /**
     * 请求参数类型错误异常的捕获
     *
     * @param e
     * @return
     */
    @ExceptionHandler(BindException.class)
    public ResponseInfo bindException(BindException e) {
        log.error("BindException: ", e);
        return new ResponseInfo<>(BaseErrorEnums.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseInfo methodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("BindException: ", e);
        List<FieldError> bindingResult = e.getBindingResult().getFieldErrors();
        StringBuffer errMsg = new StringBuffer();
        bindingResult.forEach(x -> errMsg.append(String.format("%s:%s ", x.getField(), x.getDefaultMessage())));
        return new ResponseInfo<>(BaseErrorEnums.ERROR_ARGS.getCode(), errMsg.toString());
    }

    /**
     * 404错误异常的捕获
     *
     * @param e
     * @return
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseInfo noHandlerFoundException(NoHandlerFoundException e) {
        log.error("NoHandlerFoundException ", e);
        return new ResponseInfo<>(BaseErrorEnums.NOT_FOUND);
    }

    /**
     * 自定义异常的捕获
     * 自定义抛出异常。统一的在这里捕获返回JSON格式的友好提示。
     *
     * @param exception
     * @param request
     * @return
     */
    @ExceptionHandler(value = {BaseException.class})
    public ResponseInfo baseException(BaseException exception, HttpServletRequest request) {
        String uri = request.getRequestURI();
        log.warn("occurs error when execute url ={} ,message {}", uri, exception);
        return new ResponseInfo<>(ObjectUtil.getInteger(exception.getErrCode()), exception.getMessage());
    }

    /**
     * 数据库操作出现异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(value = {SQLException.class, DataAccessException.class})
    public ResponseInfo systemError(Exception e) {
        log.error("SQLException,DataAccessException:", e);
        return new ResponseInfo<>(BaseErrorEnums.ERROR_SYS);
    }

    /**
     * 网络连接失败！
     *
     * @param e
     * @return
     */
    @ExceptionHandler(value = {ConnectException.class})
    public ResponseInfo connect(ConnectException e) {
        log.error("ConnectException ", e);
        return new ResponseInfo<>(BaseErrorEnums.CONNECTION_ERROR);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseInfo notAllowedMethod(HttpRequestMethodNotSupportedException e) {
        log.error("HttpRequestMethodNotSupportedException {}", e);
        return new ResponseInfo<>(BaseErrorEnums.BAD_REQUEST_TYPE);
    }

    @ExceptionHandler(Exception.class)
    public ResponseInfo all(Exception e) {
        log.error("Exception :", e);
        return new ResponseInfo<>(BaseErrorEnums.ERROR_SYS);
    }
}
