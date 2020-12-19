package live.lumia.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * redis 分布式锁
 *
 * @author liyuwei
 */
@Target(value = {ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RedisLock {

    /**
     * 锁名称
     *
     * @return str
     */
    String value();

    /**
     * 最长锁定时间/秒,超过该时间会自动释放锁
     *
     * @return
     */
    long maxLockTime() default 60;

    /**
     * 同步等待时间/秒，如果前1个线程执行超过该时间,当前线程抛出异常，等待时间必须小于释放锁时间
     *
     * @return
     */
    long waitTime() default 0;


}
