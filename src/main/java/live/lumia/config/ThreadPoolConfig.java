package live.lumia.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author lc
 * @date 2019/11/21下午 10:02
 */
@Slf4j
@Configuration
public class ThreadPoolConfig implements AsyncConfigurer {
    /**
     * 核心线程数
     */
    @Value("${pool.core-size:80}")
    private Integer poolCoreSize;

    /**
     * 最大线程数
     */
    @Value("${pool.max-size:200}")
    private Integer poolMaxSize;

    /**
     * 队列容量
     */
    @Value("${pool.queue-capacity:200}")
    private Integer queueCapacity;

    /**
     * 线程活跃时间（秒）
     */
    @Value("${pool.keep-alive-seconds:200}")
    private Integer keepAliveSeconds;

    /**
     * 设置默认线程名称
     */
    @Value("${pool.thread-name-prefix:async-thread-}")
    private String threadNamePrefix;

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 设置核心线程数 80
        executor.setCorePoolSize(poolCoreSize);
        // 设置最大线程数 200
        executor.setMaxPoolSize(poolMaxSize);
        // 设置队列容量 200
        executor.setQueueCapacity(queueCapacity);
        // 设置线程活跃时间（秒）
        executor.setKeepAliveSeconds(keepAliveSeconds);
        // 设置默认线程名称
        executor.setThreadNamePrefix(threadNamePrefix);
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
