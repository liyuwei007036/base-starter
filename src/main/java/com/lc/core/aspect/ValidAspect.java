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
                throw new BaseException(BaseErrorEnums.ERROR_AUTH);
            }
        }
    }

    @AfterReturning(pointcut = "cut()", returning = "responseInfo")
    public void afterRun(ResponseInfo responseInfo) {
        try {
            BaseController controller = CONTROLLER.get();
            log.info("【------------------------ success request start -------------------------】");
            log.info("url: {} ", controller.getCurUrl());
            log.info("request_ip: {}", RequestUtils.getIpAddress(controller.getRequest()));
            log.info("request_user_agent: {}", RequestUtils.getUserAgent(controller.getRequest()));
            log.info("request_args: {}", JSON.toJSONString(ARGS.get()));
            log.info("request_user: {}", controller.getCurrentUser());
            log.info("request_token: {}", controller.getRequest().getHeader(CommonConstant.SESSION_NAME));
            log.info("request_u-info: {}", controller.getRequest().getHeader(""));
            log.info("response_token: {}", controller.getResponse().getHeader(CommonConstant.SESSION_NAME));
            log.info("response_args: {}", responseInfo);
            log.info("【------------------------ success request end ---------------------------】\n\n");
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
        try {
            BaseController controller = CONTROLLER.get();
            log.warn("【------------------------ fail request start -------------------------】");
            log.warn("url: {} ", controller.getCurUrl());
            log.warn("request_ip: {}", RequestUtils.getIpAddress(controller.getRequest()));
            log.warn("request_user_agent: {}", RequestUtils.getUserAgent(controller.getRequest()));
            log.warn("request_args: {}", JSON.toJSONString(ARGS.get()));
            log.warn("request_user: {}", controller.getCurrentUser());
            log.warn("request_token: {}", controller.getRequest().getHeader(CommonConstant.SESSION_NAME));
            log.warn("request_u-info: {}", controller.getRequest().getHeader(""));
            log.warn("response_token: {}", controller.getResponse().getHeader(CommonConstant.SESSION_NAME));
            log.warn("response_msg: {}", e.getMessage());
            log.warn("【------------------------ fail request end ---------------------------】\n\n");
        } catch (Exception error) {
            log.error(error.getMessage(), error);
        } finally {
            CONTROLLER.get().removeThread();
            MDC.remove(UNIQUE_ID);
            recycleThread();
        }
    }
}
