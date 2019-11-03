package com.lc.common.utils;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.log4j.Log4j2;
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
import java.util.HashMap;
import java.util.Map;

@Log4j2
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
        for (String m : head.keySet()) {
            headers.add(m, head.get(m));
        }
        String ct = ObjectUtil.getString(head.get("Content-Type"));
        HttpEntity r;
        // 根据不同的请求头发送
        if (MediaType.APPLICATION_FORM_URLENCODED_VALUE.equals(ct)) {
            MultiValueMap<String, Object> postParameters = new LinkedMultiValueMap<>();
            for (String m : data.keySet()) {
                postParameters.add(m, data.get(m));
            }
            r = new HttpEntity<>(postParameters, headers);
        } else {
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
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        return post(url, data, headers);
    }

    public static String readData(HttpServletRequest request) {
        BufferedReader br = null;
        try {
            br = request.getReader();
            String line = br.readLine();
            String var4;
            if (line == null) {
                var4 = "";
                return var4;
            } else {
                StringBuilder ret = new StringBuilder();
                ret.append(line);

                while ((line = br.readLine()) != null) {
                    ret.append('\n').append(line);
                }

                var4 = ret.toString();
                return var4;
            }
        } catch (IOException var14) {
            throw new RuntimeException(var14);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException var13) {
                    log.error(var13.getMessage(), var13);
                }
            }

        }
    }

    public static JSONObject readData(ServletInputStream ris) throws IOException {
        ByteArrayOutputStream sout = new ByteArrayOutputStream();
        int b;
        while ((b = ris.read()) != -1) {
            sout.write(b);
        }
        byte[] temp = sout.toByteArray();
        String sOk = new String(temp, StandardCharsets.UTF_8);
        return JSONObject.parseObject(sOk);
    }
}
