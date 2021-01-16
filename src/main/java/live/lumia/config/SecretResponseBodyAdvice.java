package live.lumia.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import live.lumia.annotations.Secret;
import live.lumia.dto.ResponseInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @author liyuwei
 */
@RestControllerAdvice
public class SecretResponseBodyAdvice implements ResponseBodyAdvice<ResponseInfo> {


    private static final String TENANT_ID_STR = "tenant-id";


    private DefaultAesDecrypt defaultAesDecryptImpl;

    @Autowired
    private SignConfigProperties signConfigProperties;


    @PostConstruct
    private void initAesDecrypt() throws IllegalAccessException, InstantiationException {
        if (Objects.nonNull(signConfigProperties.getAesDecryptImpl())) {
            defaultAesDecryptImpl = signConfigProperties.getAesDecryptImpl().newInstance();
        }
    }

    @Override
    public boolean supports(MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> aClass) {
        return true;
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
        String tenantCode = Objects.requireNonNull(serverHttpRequest.getHeaders().get(TENANT_ID_STR)).stream().findFirst().orElse(null);
        String encrypt = defaultAesDecryptImpl.encrypt(tenantCode, data);
        o.setData(encrypt);
        return o;
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



