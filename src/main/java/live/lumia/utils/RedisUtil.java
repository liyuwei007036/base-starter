package live.lumia.utils;

import jodd.util.CollectionUtil;
import live.lumia.enums.BaseErrorEnums;
import live.lumia.error.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.*;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author l5990
 */
@Slf4j
public class RedisUtil {

    public static RedissonClient getClient() {
        return SpringUtil.getBean(RedissonClient.class);
    }

    public static Boolean hasKey(String key) {
        return getClient().getKeys().getKeysStream().anyMatch(x -> x.equals(key));
    }

    public static <K, V> void hashPut(String key, K hasKey, V value) {
        RMap<K, V> map = getClient().getMap(key);
        map.put(hasKey, value);
    }

    public static void hashPutAll(String key, Map<String, Object> data) {
        RMap<Object, Object> map = getClient().getMap(key);
        map.putAll(data);
    }

    public static void put(String key, Object value, long timeout) {
        RBucket<Object> bucket = getClient().getBucket(key);
        RFuture<Void> future = bucket.setAsync(value, timeout, TimeUnit.SECONDS);
        future.whenComplete((unused, throwable) -> {
            if (!future.isSuccess()) {
                log.error("【Redisson】保存失败");
                throw new BaseException(BaseErrorEnums.SYSTEM_ERROR);
            }
        });
    }

    public static <E> E get(String key, Class<E> clazz) {
        Object o = getClient().getBucket(key).get();
        return ModelMapperUtils.strict(o, clazz);
    }

    public static Object get(String key) {
        return getClient().getBucket(key).get();
    }

    public static <V> void setPut(String key, V values, long timeout) {
        RSet<Object> set = getClient().getSet(key);
        RFuture<Boolean> future = set.addAsync(values);
        future.whenComplete((unused, throwable) -> {
            if (!future.isSuccess()) {
                log.error("【Redisson】保存失败");
                throw new BaseException(BaseErrorEnums.SYSTEM_ERROR);
            } else {
                set.expire(timeout, TimeUnit.SECONDS);
            }
        });
    }

    public static <V> Set<V> setGet(String key, Class<V> clazz) {
        RSet<Object> set = getClient().getSet(key);
        Set<Object> objects = set.readAll();
        return objects.parallelStream().map(x -> ModelMapperUtils.strict(x, clazz)).collect(Collectors.toSet());
    }


    public static Set<Object> setGet(String key) {
        RSet<Object> set = getClient().getSet(key);
        return set.readAll();
    }

    public static <K, V> boolean hashPutIfAbsent(String key, K hasKey, V value) {
        return Objects.nonNull(getClient().getMap(key).putIfAbsent(hasKey, value));
    }

    public static <K, V> Map<K, V> hashFindAll(String key) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        return getClient().getMap(key);
    }

    public static <K, V> V hashGet(String key, K hasKey, Class<V> clazz) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        RMap<Object, Object> map = getClient().getMap(key);
        if (CollectionUtils.isEmpty(map)) {
            return null;
        }
        return ModelMapperUtils.strict(map.get(hasKey), clazz);
    }


    public static <K> Object hashGet(String key, K hasKey) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        RMap<Object, Object> map = getClient().getMap(key);
        if (CollectionUtils.isEmpty(map)) {
            return null;
        }
        return map.get(hasKey);
    }

    public static <K> void hashRemove(String key, K hasKey) {
        RMap<Object, Object> map = getClient().getMap(key);
        map.remove(hasKey);
    }


    public static Boolean remove(String key) {
        return getClient().getKeys().delete(key) == 1;
    }

    public static boolean expire(String key, long timeout) {
        return getClient().getKeys().expire(key, timeout, TimeUnit.SECONDS);
    }

}
