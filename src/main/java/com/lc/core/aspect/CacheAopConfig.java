package com.lc.core.aspect;

import com.lc.core.annotations.Cache;
import com.lc.core.service.RedisService;
import com.lc.core.utils.ObjectUtil;
import com.lc.core.utils.SpringElUtils;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @author l5990
 */
@Slf4j
@Aspect
@Component
public class CacheAopConfig {

    @Autowired
    private RedisService<String, Object> redisService;

    @Pointcut("@annotation(com.lc.core.annotations.Cache)")
    public void cacheCut() {

    }

    @Around(value = "cacheCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Cache cache = method.getAnnotation(Cache.class);
        String className = method.getDeclaringClass().getName();
        String methodName = method.getName();
        int db = cache.db();
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
                return addCache(joinPoint, db, key, condition, name, redisDataType, timeout);
            case REMOVE:
                return removeCache(joinPoint, db, key, condition, name, redisDataType);
            case UPDATE:
                return updateCache(joinPoint, db, key, condition, name, redisDataType, timeout);
            default:
                return joinPoint.proceed();
        }
    }

    private Object addCache(ProceedingJoinPoint joinPoint, int db, String key, boolean condition, String name, RedisDataType dateType, int timeout) throws Throwable {
        Object res = null;
        if (condition) {
            try {
                switch (dateType) {
                    case STRING:
                        res = redisService.get(name + "." + key, db);
                        break;
                    case SET:
                        res = redisService.setGet(name + "." + key, db);
                        break;
                    case HASH:
                        res = redisService.hashGet(name, key, db);
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                log.error("addCache fail", e);
            }

        }
        if (res == null) {
            res = joinPoint.proceed();
            setCacheByType(res, condition, name, key, db, timeout, dateType);
        }
        return res;
    }

    private Object removeCache(ProceedingJoinPoint joinPoint, int db, String key, boolean condition, String name, RedisDataType dateType) throws Throwable {
        if (condition) {
            try {
                switch (dateType) {
                    case STRING:
                    case SET:
                        redisService.remove(name + "." + key, db);
                        break;
                    case HASH:
                        redisService.hashRemove(name, key, db);
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

    private Object updateCache(ProceedingJoinPoint joinPoint, int db, String key, boolean condition, String name, RedisDataType dateType, int timeout) {
        Object res;
        try {
            res = joinPoint.proceed();
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
        setCacheByType(res, condition, name, key, db, timeout, dateType);
        return res;
    }

    private void setCacheByType(Object res, Boolean condition, String name, String key, int db, int timeout, RedisDataType dateType) {
        try {
            if (condition && res != null) {
                switch (dateType) {
                    case STRING:
                        redisService.put(name + "." + key, res, db, timeout);
                        break;
                    case SET:
                        redisService.setPut(name + "." + key, res, db, timeout);
                        break;
                    case HASH:
                        redisService.hashPut(name, key, res, db);
                        redisService.expire(name, timeout, db);
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
