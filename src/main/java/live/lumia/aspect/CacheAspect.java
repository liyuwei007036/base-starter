package live.lumia.aspect;

import live.lumia.annotations.Cache;
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
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 基于redisson 自定义缓存
 *
 * @author l5990
 */
@Slf4j
@Aspect
@Component
public class CacheAspect {

    public enum CacheMethod {
        /**
         * 缓存方法
         */
        ADD, REMOVE, UPDATE
    }

    public enum RedisDataType {
        /**
         * 缓存值类型
         */
        HASH, STRING, SET
    }


    @Pointcut("@annotation(live.lumia.annotations.Cache)")
    public void cacheCut() {

    }

    @Around(value = "cacheCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Cache cache = method.getAnnotation(Cache.class);
        String className = method.getDeclaringClass().getName();
        String methodName = method.getName();
        int timeout = cache.timeout();
        String name = cache.name();
        if (StringUtils.isEmpty(name)) {
            name = String.format("%s.%s", className, methodName);
        }
        String conditionEl = cache.condition();
        String keyEl = cache.key();
        CacheMethod type = cache.type();
        RedisDataType redisDataType = cache.dataType();
        boolean condition = true;
        if (!StringUtils.isEmpty(conditionEl)) {
            condition = ObjectUtil.getBoolean(SpringElUtils.generateKeyBySpel(conditionEl, joinPoint));
        }
        String key = ObjectUtil.getString(SpringElUtils.generateKeyBySpel(keyEl, joinPoint));
        switch (type) {
            case ADD:
                return addCache(joinPoint, key, condition, name, redisDataType, timeout);
            case REMOVE:
                return removeCache(joinPoint, key, condition, name, redisDataType);
            case UPDATE:
                return updateCache(joinPoint, key, condition, name, redisDataType, timeout);
            default:
                return joinPoint.proceed();
        }
    }

    private Object addCache(ProceedingJoinPoint joinPoint, String key, boolean condition, String name, RedisDataType dateType, int timeout) throws Throwable {
        Object res = null;
        if (condition) {
            res = getCacheByType(name, key, dateType);
        }
        if (Objects.isNull(res)) {
            // 防止缓存击穿
            RLock lock = RedisUtil.getClient().getLock(String.format("%s.%s.%s", name, key, dateType));
            boolean success = lock.tryLock(2, 3, TimeUnit.MINUTES);
            if (success) {
                try {
                    res = getCacheByType(name, key, dateType);
                    if (Objects.isNull(res)) {
                        res = joinPoint.proceed();
                        setCacheByType(res, condition, name, key, timeout, dateType);
                    }
                } finally {
                    lock.unlock();
                }
            } else {
                log.error("添加缓存时获取锁超时,lock {}", lock.getName());
            }
        }
        if (Objects.isNull(res)) {
            res = getCacheByType(name, key, dateType);
        }
        return res;
    }


    private Object removeCache(ProceedingJoinPoint joinPoint, String key, boolean condition, String name, RedisDataType dateType) throws Throwable {
        if (condition) {
            try {
                switch (dateType) {
                    case STRING:
                    case SET:
                        RedisUtil.remove(name + "." + key);
                        break;
                    case HASH:
                        RedisUtil.hashRemove(name, key);
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                log.error("removeCache error", e);
            }
        }
        return joinPoint.proceed();

    }

    private Object updateCache(ProceedingJoinPoint joinPoint, String key, boolean condition, String name, RedisDataType dateType, int timeout) {
        Object res;
        try {
            res = joinPoint.proceed();
            setCacheByType(res, condition, name, key, timeout, dateType);
        } catch (Throwable throwable) {
            log.error("更新缓存失", throwable);
            throw new RuntimeException(throwable);
        }
        return res;
    }


    private Object getCacheByType(String name, String key, RedisDataType dateType) {
        try {
            switch (dateType) {
                case STRING:
                    return RedisUtil.get(name + "." + key);
                case SET:
                    return RedisUtil.setGet(name + "." + key);
                case HASH:
                    return RedisUtil.hashGet(name, key);
                default:
                    return null;
            }
        } catch (Exception e) {
            log.error("getCacheByType fail", e);
            return null;
        }
    }

    private void setCacheByType(Object res, Boolean condition, String name, String key, int timeout, RedisDataType dateType) {
        try {
            if (condition && Objects.nonNull(res)) {
                switch (dateType) {
                    case STRING:
                        RedisUtil.put(name + "." + key, res, timeout);
                        break;
                    case SET:
                        RedisUtil.setPut(name + "." + key, res, timeout);
                        break;
                    case HASH:
                        RedisUtil.hashPut(name, key, res);
                        RedisUtil.expire(name, timeout);
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            log.error("setCacheByType error", e);
        }
    }

}
