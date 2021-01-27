package live.lumia.annotations;

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
    boolean needLogin() default false;

    /**
     * 权限代码
     *
     * @return
     */
    String code() default "";


}