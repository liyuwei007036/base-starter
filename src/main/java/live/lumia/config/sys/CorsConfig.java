package live.lumia.config.sys;

import live.lumia.config.properties.SessionNameProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * @author l5990
 */
@Configuration
public class CorsConfig {

    private CorsConfiguration buildConfig(SessionNameProperties sessionNameProperties) {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        // 允许任何域名使用
        corsConfiguration.addAllowedOrigin("http://127.0.0.1:8080");
        // 允许任何头
        corsConfiguration.addAllowedHeader("*");
        // 允许任何方法（post、get等）
        corsConfiguration.addAllowedMethod(HttpMethod.DELETE);
        corsConfiguration.addAllowedMethod(HttpMethod.PATCH);
        corsConfiguration.addAllowedMethod(HttpMethod.POST);
        corsConfiguration.addAllowedMethod(HttpMethod.GET);
        corsConfiguration.addAllowedMethod(HttpMethod.OPTIONS);
        corsConfiguration.setMaxAge(3600L);
        corsConfiguration.addExposedHeader(sessionNameProperties.getName());
        return corsConfiguration;
    }

    @Bean
    @Autowired
    public CorsFilter corsFilter(SessionNameProperties sessionNameProperties) {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 对接口配置跨域设置
        source.registerCorsConfiguration("/**", buildConfig(sessionNameProperties));
        return new CorsFilter(source);
    }
}
