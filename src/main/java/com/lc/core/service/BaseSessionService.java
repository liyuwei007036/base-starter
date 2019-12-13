package com.lc.core.service;

import com.lc.core.enums.BaseErrorEnums;
import com.lc.core.error.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author l5990
 */
@Slf4j
@Component
public class BaseSessionService<HK, HV> {

    @Autowired
    private RedisService redisService;

    /**
     * 设置 Session 值
     *
     * @param sessionId
     * @param hashKey
     * @param hashValue
     */
    public void setSessionValue(String sessionId, HK hashKey, HV hashValue, int timeOut, int dbIndex) {
        try {
            if (!redisService.hasKey(sessionId, dbIndex)) {
                redisService.hashPut(sessionId, "create_date", new Date(), dbIndex);
                redisService.expire(sessionId, timeOut, dbIndex);
            }
            redisService.hashPut(sessionId, hashKey, hashValue, dbIndex);
        } catch (Exception e) {
            e.printStackTrace();
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
    public Boolean setSessionValueIfAbsent(String sessionId, HK hashKey, HV hashValue, int timeOut, int dbIndex) {
        try {
            if (!redisService.hasKey(sessionId, dbIndex)) {
                redisService.hashPut(sessionId, "create_date", new Date(), dbIndex);
                redisService.expire(sessionId, timeOut, dbIndex);
            }
            return redisService.hashPutIfAbsent(sessionId, hashKey, hashValue, dbIndex);
        } catch (Exception e) {
            e.printStackTrace();
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
    public HV getSessionValue(String sessionId, HK hashKey, int dbIndex) {
        try {
            Object v = redisService.hashGet(sessionId, hashKey, dbIndex);
            return (HV) v;
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
    public Map<String, Object> getSessionMapBySessionId(String sessionId, int timeOut, int dbIndex) {
        if (StringUtils.isEmpty(sessionId)) {
            return new HashMap<>(16);
        }
        Map map = null;
        if (redisService.hasKey(sessionId, dbIndex)) {
            map = redisService.hashFindAll(sessionId, dbIndex);
        }
        if (map == null) {
            map = new HashMap<>(16);
        } else {
            updateAccessTime(sessionId, timeOut, dbIndex);
        }
        return map;
    }


    public Boolean updateAccessTime(String sessionId, int timeOut, int dbIndex) {
        return redisService.expire(sessionId, timeOut, dbIndex);
    }

    public HashMap getSessionList(int dbIndex) {
        return redisService.hashGetAll(dbIndex);
    }


    /**
     * 删除指定 Session
     *
     * @param sessionId
     * @return
     */
    public Boolean removeSessionId(String sessionId, int dbIndex) {
        try {
            return redisService.remove(sessionId, dbIndex);
        } catch (Exception e) {
            e.printStackTrace();
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
    public void removeSessionKey(String sessionId, HK hashKey, int dbIndex) {
        try {
            redisService.hashRemove(sessionId, hashKey, dbIndex);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("error: removeSessionKey", e);
        }
    }
}
