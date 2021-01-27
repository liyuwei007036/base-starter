package live.lumia.annotations;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author l5990
 */
@Target(value = {ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Permission {
    /**
     * 是否需要登陆
     *
     * @return
     */
    boolean needLogin() default true;

    /**
     * 权限代码
     *
     * @return
     */
    @AliasFor("value")
    String code() default "";

    /**
     * 权限代码
     *
     * @return
     */
    @AliasFor("code")
    String value() default "";

}