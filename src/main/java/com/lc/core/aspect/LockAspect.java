package com.lc.core.aspect;

import com.lc.core.annotations.RedisLock;
import com.lc.core.enums.BaseErrorEnums;
import com.lc.core.error.BaseException;
import com.lc.core.utils.ObjectUtil;
import com.lc.core.utils.RedisUtil;
import com.lc.core.utils.SpringElUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 锁切面
 */
@Slf4j
@Aspect
public class LockAspect {

    @Pointcut("@annotation(com.lc.core.annotations.RedisLock)")
    public void cut() {

    }

    @Around(value = "cut()")
    public Object around(ProceedingJoinPoint point) {
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
                } catch (Throwable throwable) {
                    throw new BaseException(BaseErrorEnums.SYSTEM_ERROR);
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
