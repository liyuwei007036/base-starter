package com.lc.common.utils;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import javax.ws.rs.core.MediaType;


/**
 * 发送短信
 */
public class MessageUtils {

    static private final String API_KEY = "key-4d7cfd85751f94b262a95d3642ce43db";
    static private final String API_SIGN = "【淘车无忧】";

    static public String sendMessage(String mobile, String message) {
        String profile_env = SpringUtil.getProperty("spring.profiles.active");
        if (profile_env.equals("dev")) return "{\"error\":0,\"message\":\"SendMessage is false\"}";
        Client client = Client.create();
        client.addFilter(new HTTPBasicAuthFilter("api", API_KEY));
        WebResource webResource = client.resource("http://sms-api.luosimao.com/v1/send.json");
        MultivaluedMapImpl formData = new MultivaluedMapImpl();
        formData.add("mobile", mobile);
        formData.add("message", message + API_SIGN);
        ClientResponse response = webResource.type(MediaType.APPLICATION_FORM_URLENCODED).
                post(ClientResponse.class, formData);
        String textEntity = response.getEntity(String.class);
        int status = response.getStatus();
        if (status == 200) {
            return textEntity;
        } else {
            return "{\"error\":-1,\"message\":\"Can not access http://sms-api.luosimao.com/v1/status.json\"}";
        }
    }

    static public String getStatus() {
        String profile_env = SpringUtil.getProperty("spring.profiles.active");
        if (profile_env.equals("dev")) return "{\"error\":0,\"message\":\"SendMessage is false\"}";
        Client client = Client.create();
        client.addFilter(new HTTPBasicAuthFilter("api", API_KEY));
        WebResource webResource = client.resource("http://sms-api.luosimao.com/v1/status.json");
        ClientResponse response = webResource.get(ClientResponse.class);
        String textEntity = response.getEntity(String.class);
        int status = response.getStatus();
        if (status == 200) {
            return textEntity;
        } else {
            return "{\"error\":-1,\"message\":\"Can not access http://sms-api.luosimao.com/v1/status.json\"}";
        }
    }
}
