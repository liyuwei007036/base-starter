package com.lc.core.utils;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * @author lc
 * @date 2019/11/21下午 10:55
 */
@Slf4j
public class SpringElUtils {

    private static SpelExpressionParser parser = new SpelExpressionParser();

    private static DefaultParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    public static Object generateKeyBySpel(String spElString, ProceedingJoinPoint joinPoint) {
        try {
            if (StringUtils.isEmpty(spElString)) {
                return null;
            }
            MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
            String[] paramNames = nameDiscoverer.getParameterNames(methodSignature.getMethod());
            if (Objects.isNull(paramNames)) {
                return null;
            }
            Expression expression = parser.parseExpression(spElString);
            EvaluationContext context = new StandardEvaluationContext();
            Object[] args = joinPoint.getArgs();
            for (int i = 0; i < args.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
            return expression.getValue(context);
        } catch (Exception e) {
            log.error("【EL表达式解析失败】", e);
            return spElString;
        }
    }
}
