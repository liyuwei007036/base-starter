package live.lumia.error;

/**
 * @author l5990
 */
public interface IErrorInterface {

    /**
     * 获取错误代码
     *
     * @return 错误码
     */
    int getCode();

    /**
     * 获取错误消息
     *
     * @return 错误消息
     */
    String getMsg();
}
