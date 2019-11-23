package com.lc.core.aspect;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lc.core.annotations.Valid;
import com.lc.core.controller.BaseController;
import com.lc.core.dto.ResponseInfo;
import com.lc.core.enums.BaseErrorEnums;
import com.lc.core.error.BaseException;
import com.lc.core.utils.HttpUtils;
import com.lc.core.utils.RequestUtils;
import com.lc.core.utils.SpringUtil;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * @author l5990
 */
@Log4j2
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

    @Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping) || @annotation(org.springframework.web.bind.annotation.GetMapping)")
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
        BaseController controller = (BaseController) o;
        CONTROLLER.set(controller);
        String args;
        List consumes = null;
        PostMapping post = method.getAnnotation(PostMapping.class);
        if (post != null) {
            consumes = Arrays.asList(post.consumes());
        }
        if (ARGS.get().length > 0 && consumes != null && !consumes.contains("multipart/form-data")) {
            args = JSON.toJSONString(ARGS.get()[0]);
        } else {
            args = "{}";
        }
        // 签名校验
        if (valid.validSign()) {
            if (args.equals(new JSONObject().toJSONString())) {
                args = HttpUtils.readData(controller.getRequest());
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
            log.info("request_token: {}", controller.getRequest().getHeader("TC5U_API"));
            log.info("request_u-info: {}", controller.getRequest().getHeader(""));
            log.info("response_token: {}", controller.getResponse().getHeader("TC5U_API"));
            log.info("response_u-info: {}", controller.getResponse().getHeader(""));
            log.info("response_args: {}", responseInfo);
            log.info("【------------------------ success request end ---------------------------】\n\n");
        } catch (Exception e) {
            log.error(e);
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
            log.warn("request_token: {}", controller.getRequest().getHeader("TC5U_API"));
            log.warn("request_u-info: {}", controller.getRequest().getHeader(""));
            log.warn("response_token: {}", controller.getResponse().getHeader("TC5U_API"));
            log.warn("response_u-info: {}", controller.getResponse().getHeader(""));
            log.warn("response_msg: {}", e.getMessage());
            log.warn("【------------------------ fail request end ---------------------------】\n\n");
        } catch (Exception error) {
            log.error(error);
        } finally {
            CONTROLLER.get().removeThread();
            MDC.remove(UNIQUE_ID);
            recycleThread();
        }
    }
}
