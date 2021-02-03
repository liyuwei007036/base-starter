package live.lumia.annotations;


import live.lumia.aspect.CacheAspect;

import java.lang.annotation.*;


/**
 * @author l5990
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Cache {

    String key();

    String name() default "";

    int timeout() default 5000;

    CacheAspect.RedisDataType dataType() default CacheAspect.RedisDataType.STRING;

    String condition() default "";

    CacheAspect.CacheMethod type() default CacheAspect.CacheMethod.ADD;
}
