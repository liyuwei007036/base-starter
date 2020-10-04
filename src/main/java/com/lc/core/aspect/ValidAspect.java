package com.lc.core.aspect;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lc.core.annotations.Valid;
import com.lc.core.controller.BaseController;
import com.lc.core.dto.ResponseInfo;
import com.lc.core.enums.BaseErrorEnums;
import com.lc.core.enums.CommonConstant;
import com.lc.core.error.BaseException;
import com.lc.core.utils.RequestUtils;
import com.lc.core.utils.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * @author l5990
 */
@Slf4j
@Aspect
@Component
public class ValidAspect {

    private static final ThreadLocal<Object[]> ARGS = new ThreadLocal<>();
    private static final ThreadLocal<Valid> VALID = new ThreadLocal<>();
    private static final ThreadLocal<BaseController> CONTROLLER = new ThreadLocal<>();

    private void recycleThread() {
        ARGS.remove();
        VALID.remove();
        CONTROLLER.remove();
    }

    @Pointcut("@annotation(org.springframework.web.bind.annotation.RequestMapping)")
    public void cut() {

    }

    private static final String UNIQUE_ID = "sessionId";

    @Before(value = "cut()")
    public void before(JoinPoint joinPoint) {
        ARGS.set(joinPoint.getArgs());
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Class<?> clazz = method.getDeclaringClass();

        Valid valid = method.getAnnotation(Valid.class);
        if (valid == null) {
            valid = clazz.getAnnotation(Valid.class);
        }
        if (valid == null) {
            log.warn("{}.{}，未添加Valid注解", clazz.getCanonicalName(), method.getName());
            return;
        }
        VALID.set(valid);
        Object o = SpringUtil.getBean(clazz);
        if (!(o instanceof BaseController)) {
            return;
        }
        BaseController controller = (BaseController) o;
        CONTROLLER.set(controller);
        String args;
        boolean flag = false;
        RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
        if (requestMapping != null) {
            flag = true;
        }
        if (flag) {
            String[] consumes = requestMapping.consumes();
            List<String> strings = Arrays.asList(consumes);
            flag = !strings.contains(MediaType.MULTIPART_FORM_DATA_VALUE);
        }
        if (ARGS.get().length > 0 && flag) {
            args = JSON.toJSONString(ARGS.get()[0]);
        } else {
            args = "{}";
        }
        // 签名校验
        if (valid.validSign()) {
            if (args.equals(new JSONObject().toJSONString())) {
                args = RequestUtils.readData(controller.getRequest());
                ARGS.set(new Object[]{args});
            }
            // todo
        }
        // 验证用户是否登陆
        if (valid.needLogin()) {
            if (!CONTROLLER.get().userHasLogin()) {
                throw new BaseException(BaseErrorEnums.ERROR_LOGIN);
            }
        }
    }

    @AfterReturning(pointcut = "cut()", returning = "responseInfo")
    public void afterRun(ResponseInfo responseInfo) {
        StringBuilder info = new StringBuilder();
        try {
            BaseController controller = CONTROLLER.get();
            info.append("\n\n【------------------------ success request start -------------------------】\n\n");
            info.append("url: ").append(controller.getCurUrl()).append("\n\n");
            info.append("request_ip: ").append(RequestUtils.getIpAddress(controller.getRequest())).append("\n\n");
            info.append("request_user_agent: ").append(RequestUtils.getUserAgent(controller.getRequest())).append("\n\n");
            info.append("request_args: ").append(JSON.toJSONString(ARGS.get())).append("\n\n");
            info.append("request_user: ").append(controller.getCurrentUser()).append("\n\n");
            info.append("request_token: ").append(controller.getRequest().getHeader(CommonConstant.SESSION_NAME)).append("\n\n");
            info.append("request_user_info: ").append(controller.getRequest().getHeader("")).append("\n\n");
            info.append("response_token: ").append(controller.getResponse().getHeader(CommonConstant.SESSION_NAME)).append("\n\n");
            info.append("response_data: ").append(JSON.toJSONString(responseInfo)).append("\n\n");
            info.append("【------------------------ success request end ---------------------------】\n\n");
            log.info(info.toString());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            CONTROLLER.get().removeThread();
            recycleThread();
            MDC.remove(UNIQUE_ID);
        }
    }

    @AfterThrowing(pointcut = "cut()", throwing = "e")
    public void afterError(Exception e) {
        StringBuilder warn = new StringBuilder();
        try {
            BaseController controller = CONTROLLER.get();
            warn.append("\n\n【------------------------ fail request start -------------------------】\n\n");
            warn.append("url: ").append(controller.getCurUrl()).append("\n\n");
            warn.append("request_ip: ").append(RequestUtils.getIpAddress(controller.getRequest())).append("\n\n");
            warn.append("request_user_agent: ").append(RequestUtils.getUserAgent(controller.getRequest())).append("\n\n");
            warn.append("request_args: ").append(JSON.toJSONString(ARGS.get())).append("\n\n");
            warn.append("request_user: ").append(controller.getCurrentUser()).append("\n\n");
            warn.append("request_token: ").append(controller.getRequest().getHeader(CommonConstant.SESSION_NAME)).append("\n\n");
            warn.append("request_user_info-info: ").append(controller.getRequest().getHeader("")).append("\n\n");
            warn.append("response_token: ").append(controller.getResponse().getHeader(CommonConstant.SESSION_NAME)).append("\n\n");
            warn.append("response_msg: ").append(e.getMessage()).append("\n\n");
            warn.append("【------------------------ fail request end ---------------------------】\n\n");
            log.error(warn.toString());
        } catch (Exception error) {
            log.error(error.getMessage(), error);
        } finally {
            CONTROLLER.get().removeThread();
            MDC.remove(UNIQUE_ID);
            recycleThread();
        }
    }
}
