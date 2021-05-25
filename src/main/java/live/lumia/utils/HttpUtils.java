package live.lumia.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.util.Optional;

/**
 * @author l5990
 */
@Slf4j
public class HttpUtils {

    private static ResponseErrorHandler getResponseErrorHandler() {
        return new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse clientHttpResponse) {
                return false;
            }

            @Override
            public void handleError(ClientHttpResponse clientHttpResponse) {

            }
        };
    }

    private static RestTemplate getRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(getResponseErrorHandler());
        try {
            restTemplate.setRequestFactory(getClientHttpRequestFactory());
        } catch (Exception e) {
            log.error("build RestTemplate error", e);
        }
        return restTemplate;

    }


    private static ClientHttpRequestFactory getClientHttpRequestFactory() throws Exception {
        TrustStrategy acceptingTrustStrategy = (x509Certificates, authType) -> true;
        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
        SSLConnectionSocketFactory connectionSocketFactory = new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
        HttpClientBuilder httpClientBuilder = HttpClients.custom();
        httpClientBuilder.setSSLSocketFactory(connectionSocketFactory);
        CloseableHttpClient httpClient = httpClientBuilder.build();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setHttpClient(httpClient);
        factory.setConnectTimeout(60000);
        return factory;
    }


    public static <T> T exchange(String url, HttpMethod method, Object data, HttpHeaders headers, Class<T> clazz) {
        RestTemplate restTemplate = getRestTemplate();
        headers = Optional.ofNullable(headers).orElseGet(() -> {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            return httpHeaders;
        });
        HttpEntity<Object> requestData = new HttpEntity<>(data, headers);
        return restTemplate.exchange(url, method, requestData, clazz).getBody();
    }

    public static <T> T exchange(String url, HttpMethod method, Class<T> clazz) {
        return exchange(url, method, null, null, clazz);
    }

    public static <T> T exchange(String url, HttpMethod method, String data, Class<T> clazz) {
        return exchange(url, method, data, null, clazz);
    }
}
