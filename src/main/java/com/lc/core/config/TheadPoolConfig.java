package com.lc.core.config;

import com.lc.core.config.properties.TheadPoolProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author lc
 * @date 2019/11/21下午 10:02
 */
@Slf4j
@EnableScheduling
@EnableConfigurationProperties(TheadPoolProperties.class)
@Configuration
public class TheadPoolConfig implements AsyncConfigurer {

    @Autowired
    private TheadPoolProperties theadPoolProperties;

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 设置核心线程数 80
        executor.setCorePoolSize(theadPoolProperties.getCorePoolSize());
        // 设置最大线程数 200
        executor.setMaxPoolSize(theadPoolProperties.getMaxPoolSize());
        // 设置队列容量 200
        executor.setQueueCapacity(theadPoolProperties.getQueueCapacity());
        // 设置线程活跃时间（秒）
        executor.setKeepAliveSeconds(theadPoolProperties.getKeepAliveSeconds());
        // 设置默认线程名称
        executor.setThreadNamePrefix(theadPoolProperties.getThreadNamePrefix());
        // 设置拒绝策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 是否允许核心线程空闲退出，默认值为false。
        executor.setAllowCoreThreadTimeOut(false);
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, objects) -> {

            log.info("throwable", throwable);
            log.info("method{}", method.getName());
            log.info("objects", objects);
        };
    }
}
