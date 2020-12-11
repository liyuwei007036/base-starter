package com.lc.core.config;

import com.lc.core.aspect.CacheAspect;
import com.lc.core.aspect.LockAspect;
import com.lc.core.aspect.ValidAspect;
import com.lc.core.error.DefaultExceptionHandler;
import com.lc.core.service.BaseSessionService;
import com.lc.core.utils.SpringUtil;
import org.redisson.Redisson;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author liyuwei
 */
@Import(value = {
        CorsConfig.class,
        LocalDateTimeSerializerConfig.class,
})
@AutoConfigureAfter(RedissonAutoConfiguration.class)
@EnableConfigurationProperties(value = {SessionNameConfig.class})
@Configuration
public class CoreAutoConfig {


    @Bean
    public ThreadPoolConfig threadPoolConfig() {
        return new ThreadPoolConfig();
    }

    @Bean
    @ConditionalOnClass(Redisson.class)
    public BaseSessionService sessionService() {
        return new BaseSessionService();
    }

    @Bean
    @ConditionalOnBean(Redisson.class)
    public CacheAspect cacheAspect() {
        return new CacheAspect();
    }

    @Bean
    @ConditionalOnBean(Redisson.class)
    public LockAspect lockAspect() {
        return new LockAspect();
    }

    @Bean
    @ConditionalOnBean(Redisson.class)
    public ValidAspect validAspect() {
        return new ValidAspect();
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
