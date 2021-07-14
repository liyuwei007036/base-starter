package live.lumia.utils;

import com.alibaba.fastjson.JSON;
import live.lumia.controller.BaseController;
import live.lumia.dto.Account;
import live.lumia.enums.BaseErrorEnums;
import live.lumia.enums.SessionConstants;
import live.lumia.error.BaseException;

import java.time.LocalDateTime;
import java.util.Date;
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
        session.put("createTime", new Date());
        session.put("loginIp", RequestUtils.getIpAddress(GlobalRequestUtils.getRequest()));
        session.put("loginAgent", RequestUtils.getUserAgent(GlobalRequestUtils.getRequest()));
        session.put(SessionConstants.USER_ID, JSON.toJSONString(account.getId()));
        session.put(SessionConstants.USER_NAME, account.getName());
        session.put(SessionConstants.USER_ACCOUNT, account.getAccount());
        RedisUtil.hashPutAll(sessionId, session);
        RedisUtil.expire(sessionId, controller.getTimeOut());
        session.clear();
    }
}
