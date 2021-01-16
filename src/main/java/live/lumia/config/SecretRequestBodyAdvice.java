package live.lumia.config;

import live.lumia.annotations.Secret;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @author liyuwei
 */
@EnableConfigurationProperties(SignConfigProperties.class)
@RestControllerAdvice
public class SecretRequestBodyAdvice implements RequestBodyAdvice {

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
    public boolean supports(MethodParameter methodParameter, Type type, Class<? extends HttpMessageConverter<?>> aClass) {
        return methodParameter.hasParameterAnnotation(RequestBody.class);
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage httpInputMessage, MethodParameter methodParameter, Type type, Class<? extends HttpMessageConverter<?>> aClass) {
        if (Objects.isNull(methodParameter.getMethod())) {
            return httpInputMessage;
        }
        Secret secret = SecretResponseBodyAdvice.getSecret(methodParameter);
        if (Objects.isNull(secret)) {
            return httpInputMessage;
        }

        if (!secret.decode()) {
            return httpInputMessage;
        }
        return new HttpInputMessage() {
            @Override
            public InputStream getBody() throws IOException {
                String tenantCode = Objects.requireNonNull(httpInputMessage.getHeaders().get(TENANT_ID_STR)).stream().findFirst().orElse(null);
                String bodyStr = IOUtils.toString(httpInputMessage.getBody(), StandardCharsets.UTF_8);
                String decrypt = defaultAesDecryptImpl.decrypt(tenantCode, bodyStr);
                return IOUtils.toInputStream(decrypt, StandardCharsets.UTF_8);
            }

            @Override
            public HttpHeaders getHeaders() {
                return httpInputMessage.getHeaders();
            }
        };

    }

    @Override
    public Object afterBodyRead(Object o, HttpInputMessage httpInputMessage, MethodParameter methodParameter, Type type, Class<? extends HttpMessageConverter<?>> aClass) {
        return o;
    }

    @Override
    public Object handleEmptyBody(Object o, HttpInputMessage httpInputMessage, MethodParameter methodParameter, Type type, Class<? extends HttpMessageConverter<?>> aClass) {
        return o;
    }
}
