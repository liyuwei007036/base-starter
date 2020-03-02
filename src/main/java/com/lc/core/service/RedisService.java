package com.lc.core.service;

import com.lc.core.config.RedisTemplate;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author l5990
 */
public class RedisService<K, V> {

    /**
     * 在构造器中获取redisTemplate实例, key(not hasKey) 默认使用String类型
     */
    private RedisTemplate redisTemplate;

    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Boolean hasKey(K key, int dbIndex) {
        redisTemplate.indexed.set(dbIndex);
        return redisTemplate.hasKey(key);
    }

    public void hashPut(String key, K hasKey, Object value, int dbIndex) {
        redisTemplate.indexed.set(dbIndex);
        redisTemplate.opsForHash().put(key, hasKey, value);
    }

    public void hashPutAll(String key, Map<String, Object> data, int dbIndex) {
        redisTemplate.indexed.set(dbIndex);
        redisTemplate.opsForHash().putAll(key, data);
    }

    public void put(String key, Object value, int dbIndex, long timeout) {
        redisTemplate.indexed.set(dbIndex);
        redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(timeout));
    }

    public Object get(String key, int dbIndex) {
        redisTemplate.indexed.set(dbIndex);
        return redisTemplate.opsForValue().get(key);
    }

    public void setPut(String key, V values, int dbIndex, long timeout) {
        redisTemplate.indexed.set(dbIndex);
        redisTemplate.opsForSet().add(key, values);
        redisTemplate.expire(key, timeout, TimeUnit.SECONDS);
    }

    public V setGet(String key, int dbIndex) {
        redisTemplate.indexed.set(dbIndex);
        return (V) redisTemplate.opsForSet().members(key);
    }

    public boolean putIfAbsent(String key, V value, int dbIndex, long timeOut) {
        redisTemplate.indexed.set(dbIndex);
        return redisTemplate.opsForValue().setIfPresent(key, value, timeOut, TimeUnit.SECONDS);
    }

    public boolean hashPutIfAbsent(String key, K hasKey, V value, int dbIndex) {
        redisTemplate.indexed.set(dbIndex);
        return redisTemplate.opsForHash().putIfAbsent(key, hasKey, value);
    }

    public Map<K, V> hashFindAll(String key, int dbIndex) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        redisTemplate.indexed.set(dbIndex);
        return (Map<K, V>) redisTemplate.opsForHash().entries(key);
    }

    public V hashGet(String key, K hasKey, int dbIndex) {
        redisTemplate.indexed.set(dbIndex);
        return (V) redisTemplate.opsForHash().get(key, hasKey);
    }

    public void hashRemove(String key, K hasKey, int dbIndex) {
        redisTemplate.indexed.set(dbIndex);
        redisTemplate.opsForHash().delete(key, hasKey);
    }


    public Boolean remove(String key, int dbIndex) {
        redisTemplate.indexed.set(dbIndex);
        return redisTemplate.delete(key);
    }

    public boolean expire(String key, long timeout, int dbIndex) {
        redisTemplate.indexed.set(dbIndex);
        return redisTemplate.expire(key, timeout, TimeUnit.SECONDS);
    }

    public HashMap<String, Object> hashGetAll(int dbIndex) {
        Set<String> keys = redisTemplate.keys("*");
        HashMap<String, Object> map = new HashMap<>(15);
        if (Objects.isNull(keys)) {
            return map;
        }
        keys.parallelStream().forEach(x -> map.put(x, hashFindAll(x, dbIndex)));
        return map;
    }

}
