package com.lc.core.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author lc
 * @date 2019/11/21下午 9:59
 */
@Data
@ConfigurationProperties(prefix = "thead.pool")
public class TheadPoolProperties {

    /**
     * 核心线程数
     */
    private Integer corePoolSize;

    /**
     * 最大线程数
     */
    private Integer maxPoolSize;

    /**
     * 队列容量
     */
    private Integer queueCapacity;

    /**
     * 线程活跃时间（秒）
     */
    private Integer keepAliveSeconds;

    /**
     * 设置默认线程名称
     */
    private String threadNamePrefix;


}
