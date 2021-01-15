package live.lumia.config;

import live.lumia.dto.BaseAesDTO;
import live.lumia.dto.ResponseInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StreamUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author liyuwei
 */
@Slf4j
@EnableConfigurationProperties(SignConfigProperties.class)
@Configuration
public class SignMessageConverter implements HttpMessageConverter {

    private static final String TENANT_ID_STR = "tenant-id";

    private static final ThreadLocal<String> TENANT_LOCAL = new ThreadLocal<>();

    private DefaultAesDecrypt defaultAesDecryptImpl;

    @PostConstruct
    private void initAesDecrypt() throws IllegalAccessException, InstantiationException {
        if (Objects.nonNull(signConfigProperties.getAesDecryptImpl())) {
            defaultAesDecryptImpl = signConfigProperties.getAesDecryptImpl().newInstance();
        }
    }

    @Autowired
    private SignConfigProperties signConfigProperties;

    @Override
    public boolean canRead(Class clazz, MediaType mediaType) {
        return BaseAesDTO.class.isAssignableFrom(clazz);
    }

    @Override
    public boolean canWrite(Class clazz, MediaType mediaType) {
        return ResponseInfo.class.isAssignableFrom(clazz);
    }

    @Override
    public List<MediaType> getSupportedMediaTypes() {
        ArrayList<MediaType> objects = new ArrayList<>();
        objects.add(MediaType.APPLICATION_JSON);
        return objects;
    }

    @Override
    public Object read(Class clazz, HttpInputMessage inputMessage) throws HttpMessageNotReadableException {
        String tenantId = "";
        List<String> tenantIds = inputMessage.getHeaders().get(TENANT_ID_STR);
        if (!CollectionUtils.isEmpty(tenantIds)) {
            tenantId = tenantIds.stream().findFirst().orElse("");
        }
        TENANT_LOCAL.set(tenantId);
        return defaultAesDecryptImpl.decrypt(tenantId, clazz, inputMessage);
    }

    @Override
    public void write(Object o, MediaType contentType, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        try {
            String result = defaultAesDecryptImpl.encrypt(TENANT_LOCAL.get(), o);
            StreamUtils.copy(result, StandardCharsets.UTF_8, outputMessage.getBody());
        } finally {
            TENANT_LOCAL.remove();
        }
    }
}
