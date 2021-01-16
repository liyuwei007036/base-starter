package live.lumia.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 加解密注解
 *
 * @author liyuwei
 */
@Target(value = {ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Secret {
    /**
     * 是否加密
     *
     * @return
     */
    boolean encode() default true;

    /**
     * 解密
     *
     * @return
     */
    boolean decode() default true;
}
