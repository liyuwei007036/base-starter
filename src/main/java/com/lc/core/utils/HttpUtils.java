package com.lc.core.utils;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author l5990
 */
@Slf4j
public final class HttpUtils {
    private static RestTemplate restTemplate = new RestTemplate();

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
        restTemplate.setErrorHandler(getResponseErrorHandler());
        return restTemplate.getForEntity(url, String.class).getBody();
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
        restTemplate.setErrorHandler(getResponseErrorHandler());
        HttpHeaders headers = new HttpHeaders();
        head.forEach(headers::add);
        String ct = ObjectUtil.getString(head.get("Content-Type"));
        HttpEntity r;
        // 根据不同的请求头发送
        if (MediaType.MULTIPART_FORM_DATA_VALUE.equals(ct) || (MediaType.APPLICATION_FORM_URLENCODED_VALUE.equals(ct))) {
            MultiValueMap<String, Object> postParameters = new LinkedMultiValueMap<>();
            for (String m : data.keySet()) {
                postParameters.add(m, data.get(m));
            }
            r = new HttpEntity<>(postParameters, headers);
        } else{
            r = new HttpEntity<>(JSONObject.toJSONString(data), headers);
        }
        return restTemplate.postForEntity(url, r, String.class).getBody();
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
