package live.lumia.config.secret;

import com.alibaba.fastjson.JSONObject;
import live.lumia.annotations.Secret;
import live.lumia.dto.ResponseInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

/**
 * @author liyuwei
 */
@ConditionalOnBean(DefaultAesDecrypt.class)
@RestControllerAdvice
public class SecretResponseBodyAdvice implements ResponseBodyAdvice<ResponseInfo> {


    private static final String TENANT_ID_STR = "tenant-id";

    @Autowired
    private DefaultAesDecrypt defaultAesDecryptImpl;

    @Override
    public boolean supports(MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> aClass) {
        Secret secret = getSecret(methodParameter);
        return Objects.nonNull(secret);
    }

    @Override
    public ResponseInfo beforeBodyWrite(ResponseInfo o, MethodParameter methodParameter, MediaType mediaType, Class<? extends HttpMessageConverter<?>> aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        Secret secret = getSecret(methodParameter);
        if (Objects.isNull(secret) || !secret.encode()) {
            return o;
        }
        Object data = Optional.ofNullable(o.getData()).orElse(new JSONObject());
        String tenantCode = getTenantIdStr(serverHttpRequest.getHeaders());
        String encrypt = defaultAesDecryptImpl.encrypt(tenantCode, data);
        o.setData(encrypt);
        return o;
    }

    public static String getTenantIdStr(HttpHeaders httpHeaders) {
        if (Objects.isNull(httpHeaders)) {
            return null;
        }
        return Optional.ofNullable(httpHeaders.get(TENANT_ID_STR)).orElse(Collections.emptyList()).stream().findFirst().orElse(null);
    }

    public static Secret getSecret(MethodParameter methodParameter) {
        Method method = methodParameter.getMethod();
        if (Objects.isNull(method)) {
            return null;
        }
        Class<?> clazz = methodParameter.getDeclaringClass();
        return Optional.ofNullable(method.getAnnotation(Secret.class)).orElse(clazz.getAnnotation(Secret.class));
    }
}



