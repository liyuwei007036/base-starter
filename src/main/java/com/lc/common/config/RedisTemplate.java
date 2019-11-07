package com.lc.common.config;

import org.springframework.data.redis.connection.RedisConnection;

/**
 * @author l5990
 */
public class RedisTemplate<HK, HV> extends org.springframework.data.redis.core.RedisTemplate {

    public static ThreadLocal<Integer> indexed = ThreadLocal.withInitial(() -> 0);

    @Override
    protected RedisConnection preProcessConnection(RedisConnection connection, boolean existingConnection) {
        try {
            Integer dbIndex = indexed.get();
            //如果设置了dbIndex
            if (dbIndex != null) {
                connection.select(dbIndex);
            } else {
                connection.select(0);
            }
        } finally {
            indexed.remove();
        }
        return super.preProcessConnection(connection, existingConnection);
    }
}
