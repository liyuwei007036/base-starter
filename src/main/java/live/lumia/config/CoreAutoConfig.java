package live.lumia.config;

import live.lumia.aspect.CacheAspect;
import live.lumia.aspect.LockAspect;
import live.lumia.aspect.PermissionAspect;
import live.lumia.config.properties.SessionNameProperties;
import live.lumia.error.DefaultExceptionHandler;
import live.lumia.utils.SpringUtil;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author liyuwei
 */
@EnableConfigurationProperties(value = {SessionNameProperties.class})
@Configuration
public class CoreAutoConfig {

    @Bean
    public CacheAspect cacheAspect() {
        return new CacheAspect();
    }

    @Bean
    public LockAspect lockAspect() {
        return new LockAspect();
    }

    @Bean
    public PermissionAspect validAspect() {
        return new PermissionAspect();
    }

    @Bean
    public DefaultExceptionHandler defaultExceptionHandler() {
        return new DefaultExceptionHandler();
    }

    @Bean
    public SpringUtil springUtil() {
        return new SpringUtil();
    }

}
