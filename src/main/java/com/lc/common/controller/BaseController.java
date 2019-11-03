package com.lc.common.controller;


import com.lc.common.service.SessionService;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author l5990
 */
public abstract class BaseController {

    @Autowired
    private SessionService SessionService;

    private static final String UNIQUE_ID = "sessionId";


    private static final ThreadLocal<String> SESSION_ID = new ThreadLocal<>();
    private static final ThreadLocal<HttpServletRequest> LOCAL_REQUEST = new ThreadLocal<>();
    private static final ThreadLocal<HttpServletResponse> LOCAL_RESPONSE = new ThreadLocal<>();
    private static final ThreadLocal<Map<String, Object>> TMP_SESSION_MAP = new ThreadLocal<>();


    @ModelAttribute
    public void setHttp(HttpServletRequest request, HttpServletResponse response) {
        LOCAL_REQUEST.set(request);
        LOCAL_RESPONSE.set(response);
        loadSessionId();
        insertMdc();
    }

    private boolean insertMdc() {
        UUID uuid = UUID.randomUUID();
        String uniqueId = uuid.toString().replace("-", "");
        MDC.put(UNIQUE_ID, uniqueId);
        return true;
    }

    public String getSessionId() {
        return SESSION_ID.get();
    }

    public void loadSessionId() {
        SESSION_ID.set(getSessionId(LOCAL_REQUEST.get(), getSessionType()));
        checkSession();
        Map map = SessionService.getSessionMapBySessionId(SESSION_ID.get(), this.getTimeOut(), getDbIndex());
        TMP_SESSION_MAP.set(map);
    }

    public String getSessionId(HttpServletRequest request, String sessionType) {
        String session_id = request.getHeader(sessionType);
        if (session_id != null) {
            session_id = session_id.toLowerCase().trim();
        }
        return session_id;
    }

    public abstract String getSessionType();

    public abstract int getTimeOut();

    public abstract int getDbIndex();

    /**
     * 设置 session 变量
     *
     * @param key
     * @param value
     */
    public void setSessionAttr(String key, Object value) {
        checkSession();
        if (value != null) {
            Map<String, Object> session_map = TMP_SESSION_MAP.get();
            session_map.put(key, value);
            TMP_SESSION_MAP.set(session_map);
            SessionService.setSessionValue(SESSION_ID.get(), key, value, getTimeOut(), getDbIndex());
        }
    }

    private void checkSession() {
        if (StringUtils.isEmpty(SESSION_ID.get())) {
            String session_id = UUID.randomUUID().toString().toLowerCase();
            SESSION_ID.set(session_id);
            this.getResponse().setHeader(getSessionType(), session_id);
        }
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
    public <T> T getSessionAttr(String key) {
        if (StringUtils.isEmpty(SESSION_ID.get())) return null;
        Object session = TMP_SESSION_MAP.get().get(key);
        if (session == null) return null;
        return (T) session;
    }

    /**
     * 删除session
     *
     * @param key
     */
    public void removeSession(String key) {
        SessionService.removeSessionKey(getSessionId(), key, getDbIndex());
    }


    /**
     * 删除session
     */
    public void removeSession() {
        SessionService.removeSessionId(getSessionId(), getDbIndex());
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

    /**
     * 设置 Session 变量
     *
     * @param sessionParams
     */
    public void setSessionParams(Map<String, String[]> sessionParams) {
        Map<String, String> m = new HashMap<>();
        for (String key : sessionParams.keySet()) {
            String[] value = sessionParams.get(key);
            if (value != null) {
                if (value.length == 1) {
                    m.put(key, value[0]);
                } else if (value.length > 1) {
                    m.put(key, org.apache.commons.lang.StringUtils.join(value, ','));
                }
            }
        }
        this.setSessionAttr(this.getClass().getName(), m);
    }
}
