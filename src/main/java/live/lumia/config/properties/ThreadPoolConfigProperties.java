package live.lumia.config.properties;

import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Objects;

/**
 * @author liyuwei
 */
@ConfigurationProperties("sys.pool")
@Data
public class ThreadPoolConfigProperties implements InitializingBean {

    /**
     * 核心线程数
     */
    private Integer coreSize;

    /**
     * 最大线程数
     */
    private Integer maxSize;

    /**
     * 队列容量
     */
    private Integer capacity;

    /**
     * 线程活跃时间（秒）
     */
    private Integer keepAliveSeconds;

    /**
     * 设置默认线程名称
     */
    private String threadNamePrefix;

    @Override
    public void afterPropertiesSet() {
        if (Objects.isNull(coreSize)) {
            coreSize = 80;
        }
        if (Objects.isNull(maxSize)) {
            maxSize = 200;
        }
        if (Objects.isNull(capacity)) {
            capacity = 200;
        }
        if (Objects.isNull(keepAliveSeconds)) {
            keepAliveSeconds = 200;
        }
        if (Objects.isNull(threadNamePrefix)) {
            threadNamePrefix = "async-thread-";
        }
    }
}
