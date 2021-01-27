package live.lumia.aspect;

import live.lumia.annotations.RedisLock;
import live.lumia.enums.BaseErrorEnums;
import live.lumia.error.BaseException;
import live.lumia.utils.ObjectUtil;
import live.lumia.utils.RedisUtil;
import live.lumia.utils.SpringElUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 锁切面
 */
@Slf4j
@Aspect
@Component
public class LockAspect {

    @Pointcut("@annotation(live.lumia.annotations.RedisLock)")
    public void cut() {

    }

    @Around(value = "cut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        RedisLock redisLock = method.getAnnotation(RedisLock.class);
        String key = ObjectUtil.getString(SpringElUtils.generateKeyBySpel(redisLock.value(), point));
        if (StringUtils.isEmpty(key)) {
            log.error("The value of redis distribution lock key is empty");
        }
        RLock lock = RedisUtil.getClient().getLock(key);
        try {
            boolean res = lock.tryLock(redisLock.waitTime(), redisLock.maxLockTime(), TimeUnit.SECONDS);
            if (res) {
                try {
                    return point.proceed();
                } finally {
                    lock.unlock();
                }
            } else {
                throw new BaseException(BaseErrorEnums.ERROR_SYS);
            }
        } catch (Exception e) {
            throw new BaseException(BaseErrorEnums.SYSTEM_ERROR);
        }
    }
}
