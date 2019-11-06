package com.lc.common.annotations;


import com.lc.common.aspect.CacheMethod;
import com.lc.common.aspect.RedisDataType;

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

    int db() default 9;

    int timeout() default 5000;

    RedisDataType dataType() default RedisDataType.STRING;

    String condition() default "";

    CacheMethod type() default CacheMethod.ADD;
}
