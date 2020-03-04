package com.lc.core.utils;

import com.alibaba.fastjson.JSON;
import com.lc.core.controller.BaseController;
import com.lc.core.dto.Account;
import com.lc.core.enums.BaseErrorEnums;
import com.lc.core.enums.SessionConstants;
import com.lc.core.error.BaseException;
import com.lc.core.service.RedisService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lc
 * @date 2020/3/2下午 9:46
 */
public class LoginUtils {

    public static void doUserLogin(Account account, BaseController controller) {
        if (account == null) {
            throw new BaseException(BaseErrorEnums.SYSTEM_ERROR);
        }
        String sessionId = controller.getSessionId();
        Map<String, Object> session = new HashMap<>(16);
        session.put(SessionConstants.USER, JSON.toJSONString(account));
        session.put("createTime", LocalDateTime.now());
        session.put(SessionConstants.USER_ID, JSON.toJSONString(account.getId()));
        session.put(SessionConstants.USER_NAME, account.getName());
        session.put(SessionConstants.USER_ACCOUNT, account.getAccount());
        RedisService bean = SpringUtil.getBean(RedisService.class);
        bean.hashPutAll(sessionId, session, controller.getDbIndex());
        bean.expire(sessionId, controller.getTimeOut(), controller.getDbIndex());
        session.clear();
    }
}
