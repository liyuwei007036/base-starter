package live.lumia.utils;

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

    private static final SpelExpressionParser PARSER = new SpelExpressionParser();

    private static final DefaultParameterNameDiscoverer NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

    public static Object generateKeyBySpel(String spElString, ProceedingJoinPoint joinPoint) {
        try {
            if (StringUtils.isEmpty(spElString)) {
                return null;
            }
            MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
            String[] paramNames = NAME_DISCOVERER.getParameterNames(methodSignature.getMethod());
            if (Objects.isNull(paramNames)) {
                return null;
            }
            Expression expression = PARSER.parseExpression(spElString);
            EvaluationContext context = new StandardEvaluationContext();
            Object[] args = joinPoint.getArgs();
            for (int i = 0; i < args.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
            return expression.getValue(context);
        } catch (Exception e) {
            log.error("【EL表达式解析失败】");
            return spElString;
        }
    }
}
