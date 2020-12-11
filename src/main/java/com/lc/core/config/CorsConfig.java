package com.lc.core.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * @author l5990
 */
@Configuration
public class CorsConfig {

    private CorsConfiguration buildConfig(SessionNameConfig sessionNameConfig) {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        // 允许任何域名使用
        corsConfiguration.addAllowedOrigin("*");
        // 允许任何头
        corsConfiguration.addAllowedHeader("*");
        // 允许任何方法（post、get等）
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.setMaxAge(3600L);
        corsConfiguration.addExposedHeader(sessionNameConfig.getName());
        return corsConfiguration;
    }

    @Bean
    @Autowired
    public CorsFilter corsFilter(SessionNameConfig sessionNameConfig) {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 对接口配置跨域设置
        source.registerCorsConfiguration("/**", buildConfig(sessionNameConfig));
        return new CorsFilter(source);
    }
}
