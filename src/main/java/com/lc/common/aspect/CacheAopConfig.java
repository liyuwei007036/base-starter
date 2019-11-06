package com.lc.common.aspect;

import com.lc.common.annotations.Cache;
import com.lc.common.service.RedisService;
import com.lc.common.utils.ObjectUtil;
import lombok.extern.log4j.Log4j2;
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

@Log4j2
@Aspect
@Component
public class CacheAopConfig {

    @Autowired
    private RedisService<String, Object> redisService;

    @Pointcut("@annotation(com.tc5u.common.annotations.TC5UCache)")
    public void cacheCut() {

    }

    @Around(value = "cacheCut()")
    public Object around(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Cache cache = method.getAnnotation(Cache.class);
        String class_name = method.getDeclaringClass().getName();
        String method_name = method.getName();
        int db = cache.db();
        int timeout = cache.timeout();
        String name = cache.name();
        if (StringUtils.isEmpty(name)) {
            name = String.format("%s.%s", class_name, method_name);
        }
        String conditionEL = cache.condition();
        String keyEL = cache.key();
        CacheMethod type = cache.type();
        RedisDataType redisDataType = cache.dataType();
        Boolean condition = true;
        if (!StringUtils.isEmpty(conditionEL)) {
            condition = ObjectUtil.getBoolean(generateKeyBySpEL(conditionEL, joinPoint));
        }
        String key = ObjectUtil.getString(generateKeyBySpEL(keyEL, joinPoint));
        switch (type) {
            case ADD:
                return addCache(joinPoint, db, key, condition, name, redisDataType, timeout);
            case REMOVE:
                return removeCache(joinPoint, db, key, condition, name, redisDataType);
            case UPDATE:
                return updateCache(joinPoint, db, key, condition, name, redisDataType, timeout);
            default:
                try {
                    return joinPoint.proceed();
                } catch (Throwable throwable) {
                    throw new RuntimeException(throwable);
                }
        }
    }

    private Object addCache(ProceedingJoinPoint joinPoint, int db, String key, boolean condition, String name, RedisDataType data_type, int timeout) {
        Object res = null;
        if (condition) {
            try {
                switch (data_type) {
                    case STRING:
                        res = redisService.get(name + "." + key, db);
                        break;
                    case SET:
                        res = redisService.setGet(name + "." + key, db);
                        break;
                    case HASH:
                        res = redisService.hashGet(name, key, db);
                        break;
                }
            } catch (Exception e) {
                log.error(e);
            }

        }
        try {
            if (res == null) {
                res = joinPoint.proceed();
                setCacheByType(res, condition, name, key, db, timeout, data_type);
            }
            return res;
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    private Object removeCache(ProceedingJoinPoint joinPoint, int db, String key, boolean condition, String name, RedisDataType dat_type) {
        if (condition) {
            try {
                switch (dat_type) {
                    case STRING:
                        redisService.remove(name + "." + key, db);
                        break;
                    case SET:
                        redisService.remove(name + "." + key, db);
                        break;
                    case HASH:
                        redisService.hashRemove(name, key, db);
                        break;
                }
            } catch (Exception e) {
                log.error(e);
            }
        }
        try {
            return joinPoint.proceed();
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    private Object updateCache(ProceedingJoinPoint joinPoint, int db, String key, boolean condition, String name, RedisDataType data_type, int timeout) {
        Object res;
        try {
            res = joinPoint.proceed();
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
        setCacheByType(res, condition, name, key, db, timeout, data_type);
        return res;
    }

    private void setCacheByType(Object res, Boolean condition, String name, String key, int db, int timeout, RedisDataType data_type) {
        try {
            if (condition && res != null) {
                switch (data_type) {
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
                }
            }
        } catch (Exception e) {
            log.error(e);
        }
    }

    private SpelExpressionParser parser = new SpelExpressionParser();

    private DefaultParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    private Object generateKeyBySpEL(String spELString, ProceedingJoinPoint joinPoint) {
        try {
            if (StringUtils.isEmpty(spELString)) {
                return null;
            }
            MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
            String[] paramNames = nameDiscoverer.getParameterNames(methodSignature.getMethod());
            Expression expression = parser.parseExpression(spELString);
            EvaluationContext context = new StandardEvaluationContext();
            Object[] args = joinPoint.getArgs();
            for (int i = 0; i < args.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
            return expression.getValue(context);
        } catch (Exception e) {
            log.error(e);
            return spELString;
        }
    }
}
