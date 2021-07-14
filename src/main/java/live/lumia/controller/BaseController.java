package live.lumia.controller;


import com.alibaba.fastjson.JSON;
import live.lumia.config.properties.SessionNameProperties;
import live.lumia.dto.Account;
import live.lumia.enums.SessionConstants;
import live.lumia.service.BaseSessionService;
import live.lumia.utils.GlobalRequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * @author l5990
 */
public abstract class BaseController {

    @Autowired
    private BaseSessionService baseSessionService;


    @Autowired
    private SessionNameProperties sessionNameProperties;


    private static final ThreadLocal<String> SESSION_ID = new ThreadLocal<>();


    public void removeThread() {
        SESSION_ID.remove();

    }

    @ModelAttribute
    public void setHttp() {
        loadSessionId();
    }

    public String getSessionId() {
        return SESSION_ID.get();
    }

    public void loadSessionId() {
        SESSION_ID.set(getSessionId(GlobalRequestUtils.getRequest(), getSessionType()));
        Map<String, Object> map = baseSessionService.getSessionMapBySessionId(SESSION_ID.get(), this.getTimeOut());
        map.forEach(GlobalRequestUtils::setData);
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
        if (StringUtils.hasText(sessionId)) {
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
        return sessionNameProperties.getName();
    }

    /**
     * 超时时间
     *
     * @return
     */
    public Long getTimeOut() {
        return sessionNameProperties.getTimeout();
    }


    /**
     * 设置 session 变量
     *
     * @param key
     * @param value
     */
    public void setSessionAttr(String key, Object value) {
        checkSession();
        Optional.ofNullable(value).ifPresent(x -> {
            GlobalRequestUtils.setData(key, value);
            baseSessionService.setSessionValue(SESSION_ID.get(), key, x, getTimeOut());
        });
    }

    private void checkSession() {
        boolean addSession = StringUtils.isEmpty(SESSION_ID.get()) || !userHasLogin();
        if (addSession) {
            String sessionId = UUID.randomUUID().toString().toLowerCase();
            SESSION_ID.set(sessionId);
            GlobalRequestUtils.getResponse().setHeader(getSessionType(), sessionId);
        }
    }


    public Account getCurrentUser() {
        return GlobalRequestUtils.getData(SessionConstants.USER, Account.class);
    }

    public boolean userHasLogin() {
        return Objects.nonNull(getCurrentUser());
    }


    /**
     * 取得 session 变量值
     *
     * @param key
     * @param <T>
     * @return
     */
    public <T> T getSessionAttr(String key, Class<T> clazz) {
        if (!StringUtils.hasText(SESSION_ID.get())) {
            return null;
        }
        return GlobalRequestUtils.getData(key, clazz);
    }


    /**
     * 取得 session 变量值
     *
     * @param key
     * @return
     */
    public Object getSessionAttr(String key) {
        if (!StringUtils.hasText(SESSION_ID.get())) {
            return null;
        }
        return GlobalRequestUtils.getData(key, Object.class);
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
        HttpServletRequest request = GlobalRequestUtils.getRequest();
        String reqStr = request.getRequestURL().toString();
        String queryStr = request.getQueryString();
        if (StringUtils.hasText(queryStr)) {
            reqStr = reqStr + "?" + queryStr;
        }
        return reqStr;
    }

}
