package live.lumia.service;

import live.lumia.enums.BaseErrorEnums;
import live.lumia.error.BaseException;
import live.lumia.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

/**
 * @author l5990
 */
@ConditionalOnClass(Redisson.class)
@Component
@Slf4j
public class BaseSessionService {


    /**
     * 设置 Session 值
     *
     * @param sessionId
     * @param hashKey
     * @param hashValue
     */
    public <HK, HV> void setSessionValue(String sessionId, HK hashKey, HV hashValue, long timeOut) {
        try {
            if (!RedisUtil.hasKey(sessionId)) {
                RedisUtil.hashPut(sessionId, "createTime", new Date());
                RedisUtil.expire(sessionId, timeOut);
            }
            RedisUtil.hashPut(sessionId, hashKey, hashValue);
        } catch (Exception e) {
            log.error("插入session 失败", e);
            throw new BaseException(BaseErrorEnums.ERROR_SYS);
        }
    }

    /**
     * 设置 Session 值 只有key 不存在才能设置成功
     *
     * @param sessionId
     * @param hashKey
     * @param hashValue
     */
    public <HK, HV> Boolean setSessionValueIfAbsent(String sessionId, HK hashKey, HV hashValue, int timeOut) {
        try {
            if (!RedisUtil.hasKey(sessionId)) {
                RedisUtil.hashPut(sessionId, "createTime", new Date());
                RedisUtil.expire(sessionId, timeOut);
            }
            return RedisUtil.hashPutIfAbsent(sessionId, hashKey, hashValue);
        } catch (Exception e) {
            log.error("插入session 失败", e);
            throw new BaseException(BaseErrorEnums.ERROR_SYS);
        }
    }

    /**
     * 取得 Session
     *
     * @param
     * @param sessionId
     * @param hashKey
     * @return
     */
    public <HK, HV> HV getSessionValue(String sessionId, HK hashKey, Class<HV> clazz) {
        try {
            return RedisUtil.hashGet(sessionId, hashKey, clazz);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Can't get Session Value.", e);
            return null;
        }
    }

    /**
     * 取得指定 Session 集合
     *
     * @param sessionId
     * @return
     */
    public Map<String, Object> getSessionMapBySessionId(String sessionId, long timeOut) {
        if (!StringUtils.hasText(sessionId)) {
            return Collections.emptyMap();
        }
        Map<String, Object> map = null;
        if (RedisUtil.hasKey(sessionId)) {
            map = RedisUtil.hashFindAll(sessionId);
        }
        if (map == null) {
            map = Collections.emptyMap();
        } else {
            updateAccessTime(sessionId, timeOut);
        }
        return map;
    }


    public void updateAccessTime(String sessionId, long timeOut) {
        RedisUtil.expire(sessionId, timeOut);
    }


    /**
     * 删除指定 Session
     *
     * @param sessionId
     * @return
     */
    public Boolean removeSessionId(String sessionId) {
        try {
            return RedisUtil.remove(sessionId);
        } catch (Exception e) {
            log.error("error: removeSessionId", e);
            return false;
        }
    }

    /**
     * 删除指定 Session
     *
     * @param sessionId
     * @param hashKey
     * @return
     */
    public <HK> void removeSessionKey(String sessionId, HK hashKey) {
        try {
            RedisUtil.hashRemove(sessionId, hashKey);
        } catch (Exception e) {
            log.error("error: removeSessionKey", e);
        }
    }
}
