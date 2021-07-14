package live.lumia.aspect;

import com.alibaba.fastjson.JSON;
import live.lumia.annotations.Permission;
import live.lumia.controller.BaseController;
import live.lumia.dto.Account;
import live.lumia.dto.ResponseInfo;
import live.lumia.enums.BaseErrorEnums;
import live.lumia.error.BaseException;
import live.lumia.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;

/**
 * @author l5990
 */
@Slf4j
@Aspect
@Component
public class PermissionAspect {

    private static final ThreadLocal<Object[]> ARGS = new ThreadLocal<>();
    private static final ThreadLocal<BaseController> CONTROLLER = new ThreadLocal<>();

    private void recycleThread() {
        CONTROLLER.get().removeThread();
        ARGS.remove();
        CONTROLLER.remove();
    }

    @Pointcut("this(live.lumia.controller.BaseController) && execution(* (!live.lumia.controller.BaseController).*(..)) && execution(public * * (..))")
    public void cut() {

    }

    @Before(value = "cut()")
    public void before(JoinPoint point) {
        ARGS.set(point.getArgs());
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        Class<?> clazz = method.getDeclaringClass();
        Permission permission = method.getAnnotation(Permission.class);
        permission = Optional.ofNullable(permission).orElse(clazz.getAnnotation(Permission.class));
        Object o = SpringUtil.getBean(clazz);
        BaseController controller = (BaseController) o;
        CONTROLLER.set(controller);
        Optional.ofNullable(permission)
                .ifPresent(p -> {
                    // 验证用户是否登陆
                    if (p.needLogin() && !controller.userHasLogin()) {
                        throw new BaseException(BaseErrorEnums.ERROR_LOGIN);
                    }
                    // 权限校验
                    if (!StringUtils.isEmpty(p.code())) {
                        String code = p.code();
                        Account currentUser = controller.getCurrentUser();
                        boolean contains = currentUser.getPowers().contains(code);
                        if (!contains && !ObjectUtil.getBoolean(currentUser.getHasAllPowers())) {
                            throw new BaseException(BaseErrorEnums.ERROR_AUTH);
                        }
                    }
                    Account currentUser = controller.getCurrentUser();
                    if (Objects.nonNull(currentUser)) {
                        String curIpAddress = RequestUtils.getIpAddress(GlobalRequestUtils.getRequest());
                        String curUserAgent = RequestUtils.getUserAgent(GlobalRequestUtils.getRequest());
                        String loginIp = controller.getSessionAttr("loginIp").toString();
                        String loginAgent = controller.getSessionAttr("loginAgent").toString();
                        if (!curIpAddress.equals(loginIp) || !curUserAgent.equals(loginAgent)) {
                            log.warn("检测到用户登录地址和当前请求地址不一致，当前用户可能为非法用户，用户信息：{}:{}", currentUser.getAccount(), currentUser.getName());
                        }
                    }
                });
    }

    @AfterReturning(pointcut = "cut()", returning = "responseInfo")
    public void afterRun(ResponseInfo responseInfo) {
        try {
            String info = buildLog("SUCCESS", JSON.toJSONString(responseInfo));
            log.info(info);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            recycleThread();
        }
    }

    @AfterThrowing(pointcut = "cut()", throwing = "e")
    public void afterError(Throwable e) {
        try {
            String warn = buildLog("ERROR", e.getMessage());
            log.error(warn);
        } catch (Exception error) {
            log.error(error.getMessage(), error);
        } finally {
            recycleThread();
        }
    }


    private String buildLog(String success, String msg) {
        BaseController controller = CONTROLLER.get();
        String line = "\n\n";
        StringBuilder info = new StringBuilder();
        info.append(line).append("【------------------------ ").append(success).append(" REQUEST START-------------------------】").append(line);
        info.append("url: ").append(controller.getCurUrl()).append(line);
        String ipAddress = RequestUtils.getIpAddress(GlobalRequestUtils.getRequest());
        info.append("requestIp: ").append(ipAddress).append(" ").append(IPV4Utils.getLocationAndOperator(ipAddress)).append(line);
        info.append("requestArgs: ").append(JSON.toJSONString(ARGS.get())).append(line);
        info.append("requestUserAgent: ").append(RequestUtils.getUserAgent(GlobalRequestUtils.getRequest())).append(line);
        info.append("requestUser: ").append(JSON.toJSONString(controller.getCurrentUser())).append(line);
        info.append("response: ").append(msg).append(line);
        info.append("【------------------------ ").append(success).append(" REQUEST END----------------------------】").append(line);
        return info.toString();
    }
}
