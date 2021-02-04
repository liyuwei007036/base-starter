package live.lumia.config.secret;

/**
 * 默认的AES解密
 *
 * @author liyuwei
 */
public interface DefaultAesDecrypt {

    /**
     * 解密
     *
     * @param telnetId     租户ID
     * @param inputMessage 消息
     * @return 解密后的对象
     */
    String decrypt(String telnetId, String inputMessage);

    /**
     * 加密
     *
     * @param telnetId 租户ID
     * @param o        加密对象
     * @return 加密后字符串
     */
    String encrypt(String telnetId, Object o);
}
