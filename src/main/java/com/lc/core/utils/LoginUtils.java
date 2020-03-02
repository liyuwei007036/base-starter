package com.lc.core.utils;

import com.alibaba.fastjson.JSON;
import com.lc.core.controller.BaseController;
import com.lc.core.dto.User;
import com.lc.core.enums.BaseErrorEnums;
import com.lc.core.enums.SessionConstants;
import com.lc.core.error.BaseException;
import com.lc.core.service.BaseSessionService;
import com.lc.core.service.RedisService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lc
 * @date 2020/3/2下午 9:46
 */
public class LoginUtils {

    public static void doUserLogin(User user, BaseController controller) {
        if (user == null) {
            throw new BaseException(BaseErrorEnums.SYSTEM_ERROR);
        }
        String sessionId = controller.getSessionId();
        Map<String, Object> session = new HashMap<>(16);
        session.put(SessionConstants.USER, JSON.toJSONString(user));
        session.put("createTime", LocalDateTime.now());
        session.put(SessionConstants.USER_ID, JSON.toJSONString(user.getId()));
        session.put(SessionConstants.USER_NAME, user.getUName());
        session.put(SessionConstants.USER_ACCOUNT, user.getAccount());
        RedisService bean = SpringUtil.getBean(RedisService.class);
        bean.hashPutAll(sessionId, session, controller.getDbIndex());
        bean.expire(sessionId, controller.getTimeOut(), controller.getDbIndex());
        session.clear();
    }
}
