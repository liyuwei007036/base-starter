package live.lumia.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Objects;

/**
 * @author liyuwei
 */
@Slf4j
@ConfigurationProperties(prefix = "sys.sign")
@Data
public class SignConfigProperties implements InitializingBean {

    private Class<? extends DefaultAesDecrypt> aesDecryptImpl;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (Objects.isNull(aesDecryptImpl)) {
            log.debug("未配置加解密实现类");
        }
    }
}
