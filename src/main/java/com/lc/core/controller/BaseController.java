package com.lc.core.controller;


import com.alibaba.fastjson.JSON;
import com.lc.core.config.SessionNameConfig;
import com.lc.core.dto.Account;
import com.lc.core.enums.SessionConstants;
import com.lc.core.service.BaseSessionService;
import com.lc.core.utils.ObjectUtil;
import com.lc.core.utils.SpringUtil;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * @author l5990
 */
public abstract class BaseController {

    @Autowired
    private BaseSessionService baseSessionService;


    @Autowired
    private SessionNameConfig sessionNameConfig;

    private static final String UNIQUE_ID = "sessionId";


    private static final ThreadLocal<String> SESSION_ID = new ThreadLocal<>();
    private static final ThreadLocal<HttpServletRequest> LOCAL_REQUEST = new ThreadLocal<>();
    private static final ThreadLocal<HttpServletResponse> LOCAL_RESPONSE = new ThreadLocal<>();
    private static final ThreadLocal<Map<String, Object>> TMP_SESSION_MAP = new ThreadLocal<>();


    public void removeThread() {
        SESSION_ID.remove();
        LOCAL_REQUEST.remove();
        LOCAL_RESPONSE.remove();
        TMP_SESSION_MAP.remove();
    }

    @ModelAttribute
    public void setHttp(HttpServletRequest request, HttpServletResponse response) {
        LOCAL_REQUEST.set(request);
        LOCAL_RESPONSE.set(response);
        loadSessionId();
        insertMdc();
    }

    private void insertMdc() {
        UUID uuid = UUID.randomUUID();
        String uniqueId = uuid.toString().replace("-", "");
        MDC.put(UNIQUE_ID, uniqueId);
    }

    public String getSessionId() {
        return SESSION_ID.get();
    }

    public void loadSessionId() {
        SESSION_ID.set(getSessionId(LOCAL_REQUEST.get(), getSessionType()));
        Map<String, Object> map = baseSessionService.getSessionMapBySessionId(SESSION_ID.get(), this.getTimeOut());
        TMP_SESSION_MAP.set(map);
        checkSession();
    }

    /**
     * 从请求中获取sessionId
     *
     * @param request
     * @param sessionType
     * @return
     */
    private String getSessionId(HttpServletRequest request, String sessionType) {
        String sessionId = request.getHeader(sessionType);
        if (!StringUtils.isEmpty(sessionId)) {
            sessionId = sessionId.toLowerCase().trim();
        }
        return sessionId;
    }

    /**
     * session 类型
     *
     * @return
     */
    public String getSessionType() {
        return sessionNameConfig.getName();
    }

    /**
     * 超时时间
     *
     * @return
     */
    public Long getTimeOut() {
        return sessionNameConfig.getTimeout();
    }


    /**
     * 设置 session 变量
     *
     * @param key
     * @param value
     */
    public void setSessionAttr(String key, Object value) {
        checkSession();
        if (value != null) {
            Map<String, Object> sessionMap = TMP_SESSION_MAP.get();
            sessionMap.put(key, value);
            TMP_SESSION_MAP.set(sessionMap);
            baseSessionService.setSessionValue(SESSION_ID.get(), key, value, getTimeOut());
        }
    }

    private void checkSession() {
        boolean addSession = true;
        if (!StringUtils.isEmpty(SESSION_ID.get()) && userHasLogin()) {
            addSession = false;
        }
        if (addSession) {
            String sessionId = UUID.randomUUID().toString().toLowerCase();
            SESSION_ID.set(sessionId);
            this.getResponse().setHeader(getSessionType(), sessionId);
        }
    }


    public Account getCurrentUser() {
        return getSessionAttr(SessionConstants.USER, Account.class);
    }

    public boolean userHasLogin() {
        return Objects.nonNull(getCurrentUser());
    }

    public HttpServletRequest getRequest() {
        return LOCAL_REQUEST.get();
    }

    public HttpServletResponse getResponse() {
        return LOCAL_RESPONSE.get();
    }


    /**
     * 取得 session 变量值
     *
     * @param key
     * @param <T>
     * @return
     */
    public <T> T getSessionAttr(String key, Class<T> clazz) {
        if (StringUtils.isEmpty(SESSION_ID.get())) {
            return null;
        }
        Object session = TMP_SESSION_MAP.get().get(key);
        if (session == null) {
            return null;
        }
        return JSON.parseObject(session.toString(), clazz);
    }

    public BaseSessionService getSessionService() {
        return baseSessionService;
    }

    /**
     * 删除session
     *
     * @param key
     */
    public void removeSession(String key) {
        baseSessionService.removeSessionKey(getSessionId(), key);
    }


    /**
     * 删除session
     */
    public void removeSession() {
        baseSessionService.removeSessionId(getSessionId());
    }


    public String getCurUrl() {
        HttpServletRequest request = LOCAL_REQUEST.get();
        String reqStr = request.getRequestURL().toString();
        String queryStr = request.getQueryString();
        if (StringUtils.isEmpty(queryStr)) {
            return reqStr;
        } else {
            return reqStr + "?" + queryStr;
        }
    }

}
