package live.lumia.config.mp;

import com.baomidou.mybatisplus.core.MybatisPlusVersion;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import live.lumia.config.properties.DbTypeProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author lc
 * @date 2020/3/2下午 9:02
 */
@ConditionalOnClass(MybatisPlusVersion.class)
@Configuration
@EnableConfigurationProperties(DbTypeProperties.class)
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(DbTypeProperties dbType) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(dbType.getType()));
        return interceptor;
    }
}
