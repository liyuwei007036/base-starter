package live.lumia.config;

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
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

/**
 * @author liyuwei
 */
@ConditionalOnBean(DefaultAesDecrypt.class)
@RestControllerAdvice
public class SecretResponseBodyAdvice implements ResponseBodyAdvice<ResponseInfo> {


    private static final String TENANT_ID_STR = "tenant-id";

    @Autowired
    private DefaultAesDecrypt defaultAesDecryptImpl;

    @Autowired
    private SignConfigProperties signConfigProperties;


    @Override
    public boolean supports(MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> aClass) {
        return Objects.nonNull(signConfigProperties.getAesDecryptImpl());
    }

    @Override
    public ResponseInfo beforeBodyWrite(ResponseInfo o, MethodParameter methodParameter, MediaType mediaType, Class<? extends HttpMessageConverter<?>> aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        Secret secret = getSecret(methodParameter);
        if (Objects.isNull(secret)) {
            return o;
        }
        if (!secret.encode()) {
            return o;
        }
        Serializable data = o.getData();
        if (Objects.isNull(data)) {
            data = new JSONObject();
        }
        String tenantCode = getTenantIdStr(serverHttpRequest.getHeaders());
        String encrypt = defaultAesDecryptImpl.encrypt(tenantCode, data);
        o.setData(encrypt);
        return o;
    }

    public static String getTenantIdStr(HttpHeaders httpHeaders) {
        if (Objects.isNull(httpHeaders)) {
            return null;
        }
        List<String> headers = httpHeaders.get(TENANT_ID_STR);
        if (CollectionUtils.isEmpty(headers)) {
            return null;
        }
        return headers.stream().findFirst().orElse(null);
    }

    public static Secret getSecret(MethodParameter methodParameter) {
        Method method = methodParameter.getMethod();
        Class<?> clazz = methodParameter.getDeclaringClass();
        Secret secret = method.getAnnotation(Secret.class);
        if (Objects.isNull(secret)) {
            secret = clazz.getAnnotation(Secret.class);
        }
        return secret;
    }
}



