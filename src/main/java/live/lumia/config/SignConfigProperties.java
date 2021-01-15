package live.lumia.config;

import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Objects;

/**
 * @author liyuwei
 */
@ConfigurationProperties(prefix = "sys.sign")
@Data
public class SignConfigProperties implements InitializingBean {

    private Class<? extends DefaultAesDecrypt> aesDecryptImpl;

    @Override
    public void afterPropertiesSet() throws Exception {
        Objects.requireNonNull(aesDecryptImpl);
    }
}
