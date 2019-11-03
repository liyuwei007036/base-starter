package com.lc.common.service;

import com.lc.common.config.RedisTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class RedisService<HK, V> {

    // 在构造器中获取redisTemplate实例, key(not hashKey) 默认使用String类型
    private RedisTemplate<String, V> redisTemplate;

    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Boolean hasKey(String key, int dbIndex) {
        redisTemplate.indexdb.set(dbIndex);
        return redisTemplate.hasKey(key);
    }

    public void hashPut(String key, HK hashKey, V value, int dbIndex) {
        redisTemplate.indexdb.set(dbIndex);
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    public void put(String key, V value, int dbIndex, long timeout) {
        redisTemplate.indexdb.set(dbIndex);
        redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(timeout));
    }

    public V get(String key, int dbIndex) {
        redisTemplate.indexdb.set(dbIndex);
        return (V) redisTemplate.opsForValue().get(key);
    }

    public void setPut(String key, V values, int dbIndex, long timeout) {
        redisTemplate.indexdb.set(dbIndex);
        redisTemplate.opsForSet().add(key, values);
        redisTemplate.expire(key, timeout, TimeUnit.SECONDS);
    }

    public V setGet(String key, int dbIndex) {
        redisTemplate.indexdb.set(dbIndex);
        return (V) redisTemplate.opsForSet().members(key);
    }

    public Boolean hashPutIfAbsent(String key, HK hashKey, V value, int dbIndex) {
        redisTemplate.indexdb.set(dbIndex);
        return redisTemplate.opsForHash().putIfAbsent(key, hashKey, value);
    }

    public Map<HK, V> hashFindAll(String key, int dbIndex) {
        redisTemplate.indexdb.set(dbIndex);
        return (Map<HK, V>) redisTemplate.opsForHash().entries(key);
    }

    public V hashGet(String key, HK hashKey, int dbIndex) {
        redisTemplate.indexdb.set(dbIndex);
        return (V) redisTemplate.opsForHash().get(key, hashKey);
    }

    public void hashRemove(String key, HK hashKey, int dbIndex) {
        redisTemplate.indexdb.set(dbIndex);
        redisTemplate.opsForHash().delete(key, hashKey);
    }


    public Boolean remove(String key, int dbIndex) {
        redisTemplate.indexdb.set(dbIndex);
        return redisTemplate.delete(key);
    }

    public boolean expire(String key, long timeout, int dbIndex) {
        redisTemplate.indexdb.set(dbIndex);
        return redisTemplate.expire(key, timeout, TimeUnit.SECONDS);
    }

    public HashMap<String, Object> hashGetAll(int dbIndex) {
        Set<String> keys = redisTemplate.keys("*");
        HashMap<String, Object> map = new HashMap<>();
        for (String key : keys) {
            Map<HK, V> v = hashFindAll(key, dbIndex);
            map.put(key, v);
        }
        return map;
    }

}
