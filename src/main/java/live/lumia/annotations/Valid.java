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
public @interface Valid {
    /**
     * 是否需要登陆
     *
     * @return
     */
    boolean needLogin() default false;

    /**
     * 是否签名校验
     *
     * @return
     */
    boolean validSign() default true;

    /**
     * 权限
     *
     * @return
     */
    String auth() default "";

    /**
     * 指定appID
     * needAppID 为 true 时有效
     *
     * @return 指定密钥
     */
    String[] appID() default {};
}