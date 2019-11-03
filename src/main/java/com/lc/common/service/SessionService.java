package com.lc.common.service;

import com.lc.common.error.BaseException;
import com.lc.common.error.BaseErrorEnums;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Log4j2
@Component
public class SessionService<HK, HV> {

    @Autowired
    private RedisService redisService;

    /**
     * 设置 Session 值
     *
     * @param session_id
     * @param hash_key
     * @param hash_value
     */
    public void setSessionValue(String session_id, HK hash_key, HV hash_value, int time_out, int dbIndex) {
        try {
            if (!redisService.hasKey(session_id, dbIndex)) {
                redisService.hashPut(session_id, "create_date", new Date(), dbIndex);
                redisService.expire(session_id, time_out, dbIndex);
            }
            redisService.hashPut(session_id, hash_key, hash_value, dbIndex);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("插入session 失败", e);
            throw new BaseException(BaseErrorEnums.ERROR_SYS);
        }
    }

    /**
     * 设置 Session 值 只有key 不存在才能设置成功
     *
     * @param session_id
     * @param hash_key
     * @param hash_value
     */
    public Boolean setSessionValueIfAbsent(String session_id, HK hash_key, HV hash_value, int time_out, int dbIndex) {
        try {
            if (!redisService.hasKey(session_id, dbIndex)) {
                redisService.hashPut(session_id, "create_date", new Date(), dbIndex);
                redisService.expire(session_id, time_out, dbIndex);
            }
            return redisService.hashPutIfAbsent(session_id, hash_key, hash_value, dbIndex);
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
     * @param session_id
     * @param hash_key
     * @return
     */
    public HV getSessionValue(String session_id, HK hash_key, int dbIndex) {
        try {
            Object v = redisService.hashGet(session_id, hash_key, dbIndex);
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
     * @param session_id
     * @return
     */
    public Map<String, Object> getSessionMapBySessionId(String session_id, int time_out, int dbIndex) {
        Map<String, Object> map = null;
        if (redisService.hasKey(session_id, dbIndex)) {
            map = redisService.hashFindAll(session_id, dbIndex);
        }
        if (map == null) {
            map = new HashMap<>();
        } else {
            updateAccessTime(session_id, time_out, dbIndex);
        }
        return map;
    }


    public Boolean updateAccessTime(String session_id, int time_out, int dbIndex) {
        return redisService.expire(session_id, time_out, dbIndex);
    }

    public Map<String, Object> getSessionList(int dbIndex) {
        return redisService.hashGetAll(dbIndex);
    }


    /**
     * 删除指定 Session
     *
     * @param session_id
     * @return
     */
    public Boolean removeSessionId(String session_id, int dbIndex) {
        try {
            return redisService.remove(session_id, dbIndex);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("error: removeSessionId", e);
            return false;
        }
    }

    /**
     * 删除指定 Session
     *
     * @param session_id
     * @param hash_key
     * @return
     */
    public void removeSessionKey(String session_id, HK hash_key, int dbIndex) {
        try {
            redisService.hashRemove(session_id, hash_key, dbIndex);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("error: removeSessionKey", e);
        }
    }
}
