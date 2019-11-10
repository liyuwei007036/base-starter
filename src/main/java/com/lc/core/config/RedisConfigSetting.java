package com.lc.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @author l5990
 */
@Data
@ConfigurationProperties(prefix = "redis")
public class RedisConfigSetting {

    /**
     * 模式
     */
    private String model;

    private List<String> nodes;

    private String masterName;

    private String hostName;

    private Integer port;

    private Integer connectTimeout;

    private Integer readTimeout;

    private Integer maxIdle;

    private Integer maxTotal;

    private Integer maxWaitMillis;

    private Integer minIdle;

    private Integer minEvictableIdleTimeMillis;

    private Integer numTestsPerEvictionRun;

    private long timeBetweenEvictionRunsMillis;

    private Boolean testOnBorrow;

    private Boolean testWhileIdle;

    private Boolean testIbOnReturn;

    private String password;
}
