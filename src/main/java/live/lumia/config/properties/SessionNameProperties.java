package live.lumia.config.properties;


import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * @author liyuwei
 */
@ConfigurationProperties(prefix = "sys.session")
@Data
public class SessionNameProperties implements InitializingBean {
    /**
     * session名称
     */
    private String name;

    /**
     * session超时时间
     */
    private Long timeout;

    @Override
    public void afterPropertiesSet() {
        if (StringUtils.isEmpty(name)) {
            name = "sessionId";
        }
        if (Objects.isNull(timeout) || timeout < 1) {
            timeout = 1800L;
        }
    }
}
