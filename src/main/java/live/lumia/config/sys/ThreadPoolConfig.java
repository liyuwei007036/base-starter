package live.lumia.config.sys;

import live.lumia.config.properties.ThreadPoolConfigProperties;
import live.lumia.utils.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author lc
 */
@EnableAsync
@Slf4j
@EnableConfigurationProperties(ThreadPoolConfigProperties.class)
@Configuration
public class ThreadPoolConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolConfigProperties threadPoolConfigProperties = SpringUtil.getBean(ThreadPoolConfigProperties.class);
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 设置核心线程数 80
        executor.setCorePoolSize(threadPoolConfigProperties.getCoreSize());
        // 设置最大线程数 200
        executor.setMaxPoolSize(threadPoolConfigProperties.getMaxSize());
        // 设置队列容量 200
        executor.setQueueCapacity(threadPoolConfigProperties.getCapacity());
        // 设置线程活跃时间（秒）
        executor.setKeepAliveSeconds(threadPoolConfigProperties.getKeepAliveSeconds());
        // 设置默认线程名称
        executor.setThreadNamePrefix(threadPoolConfigProperties.getThreadNamePrefix());
        // 设置拒绝策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 是否允许核心线程空闲退出，默认值为false。
        executor.setAllowCoreThreadTimeOut(false);
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
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
