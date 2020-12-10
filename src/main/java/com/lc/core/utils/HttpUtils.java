package com.lc.core.utils;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author l5990
 */
@Slf4j
public final class HttpUtils {

    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    private static ResponseErrorHandler getResponseErrorHandler() {
        return new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse clientHttpResponse) {
                return true;
            }

            @Override
            public void handleError(ClientHttpResponse clientHttpResponse) {

            }
        };
    }

    public static String get(String url) {
        REST_TEMPLATE.setErrorHandler(getResponseErrorHandler());
        return REST_TEMPLATE.getForEntity(url, String.class).getBody();
    }



    /**
     * 发送post请求
     *
     * @param url
     * @param data
     * @param head
     * @return
     */
    public static String post(String url, Map<String, Object> data, Map<String, String> head) {
        REST_TEMPLATE.setErrorHandler(getResponseErrorHandler());
        REST_TEMPLATE.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        HttpHeaders headers = new HttpHeaders();
        head.forEach(headers::add);
        String ct = ObjectUtil.getString(head.get(HttpHeaders.CONTENT_TYPE));

        HttpEntity r;
        // 根据不同的请求头发送
        if (MediaType.MULTIPART_FORM_DATA_VALUE.equals(ct) || (MediaType.APPLICATION_FORM_URLENCODED_VALUE.equals(ct))) {
            MultiValueMap<String, Object> postParameters = new LinkedMultiValueMap<>();
            data.forEach(postParameters::add);
            r = new HttpEntity<>(postParameters, headers);
        } else {
            r = new HttpEntity<>(JSONObject.toJSONString(data), headers);
        }
        return REST_TEMPLATE.postForEntity(url, r, String.class).getBody();
    }

    /**
     * 默认POST 请求
     *
     * @param url
     * @param data
     * @return
     */
    public static String post(String url, Map<String, Object> data) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return post(url, data, headers.toSingleValueMap());
    }


}
