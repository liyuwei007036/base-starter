package live.lumia.config;

import live.lumia.dto.BaseAesDTO;
import org.springframework.http.HttpInputMessage;

/**
 * 默认的AES解密
 */
public interface DefaultAesDecrypt {

    /**
     * 解密
     *
     * @param telnetId     租户ID
     * @param clazz        解密对象
     * @param inputMessage 消息
     * @param <T>解密类型
     * @return 解密后的对象
     */
    <T extends BaseAesDTO> T decrypt(String telnetId, Class<T> clazz, HttpInputMessage inputMessage);

    /**
     * 加密
     *
     * @param telnetId      租户ID
     * @param o             加密对象
     * @return 加密后字符串
     */
    String encrypt(String telnetId, Object o);
}
