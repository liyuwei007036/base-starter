package live.lumia.config.secret;

import live.lumia.annotations.Secret;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @author liyuwei
 */
@ConditionalOnBean(DefaultAesDecrypt.class)
@RestControllerAdvice
public class SecretRequestBodyAdvice implements RequestBodyAdvice {


    @Autowired
    private DefaultAesDecrypt defaultAesDecryptImpl;


    @Override
    public boolean supports(MethodParameter methodParameter, Type type, Class<? extends HttpMessageConverter<?>> aClass) {
        Secret secret = SecretResponseBodyAdvice.getSecret(methodParameter);
        return Objects.nonNull(secret) && methodParameter.hasParameterAnnotation(RequestBody.class);
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage httpInputMessage, MethodParameter methodParameter, Type type, Class<? extends HttpMessageConverter<?>> aClass) {
        if (Objects.isNull(methodParameter.getMethod())) {
            return httpInputMessage;
        }
        Secret secret = SecretResponseBodyAdvice.getSecret(methodParameter);
        if (Objects.isNull(secret) || !secret.decode()) {
            return httpInputMessage;
        }
        return new HttpInputMessage() {
            @Override
            public InputStream getBody() throws IOException {
                String tenantCode = SecretResponseBodyAdvice.getTenantIdStr(httpInputMessage.getHeaders());
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
