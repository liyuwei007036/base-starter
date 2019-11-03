package com.lc.common.utils;

import com.alibaba.fastjson.JSONObject;
import com.lc.common.error.BaseException;
import com.lc.common.error.BaseErrorEnums;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import sun.misc.BASE64Encoder;

import java.io.*;
import java.util.HashMap;
import java.util.Map;


/**
 * 获取token类
 *
 * @Author : WuShukai
 * @Date :2018/2/12 10:04
 */
@Log4j2
public class BaiDuOcrUtils {

    /**
     * 获取权限token
     *
     * @return 返回示例：
     * {
     * "access_token": "24.460da4889caad24cccdb1fea17221975.2592000.1491995545.282335-1234567",
     * "expires_in": 2592000
     * }
     */
    public static String getAuth() {
        // 官网获取的 API Key 更新为你注册的
        String clientId = "x6Cf9GpiGE9eEszsnH2mWZlK";
        // 官网获取的 Secret Key 更新为你注册的
        String clientSecret = "Etoy0FpWTj94AfbeS3GDuub4Q8geV7kZ";
        return getAuth(clientId, clientSecret);
    }

    /**
     * 获取API访问token
     * 该token有一定的有效期，需要自行管理，当失效时需重新获取.
     *
     * @param ak - 百度云官网获取的 API Key
     * @param sk - 百度云官网获取的 Securet Key
     * @return assess_token 示例：
     * "24.460da4889caad24cccdb1fea17221975.2592000.1491995545.282335-1234567"
     */
    private static String getAuth(String ak, String sk) {
        // 获取token地址
        String authHost = "https://aip.baidubce.com/oauth/2.0/token?";
        String getAccessTokenUrl = authHost
                // 1. grant_type为固定参数
                + "grant_type=client_credentials"
                // 2. 官网获取的 API Key
                + "&client_id=" + ak
                // 3. 官网获取的 Secret Key
                + "&client_secret=" + sk;
        try {
            String result = HttpUtils.get(getAccessTokenUrl);
            JSONObject jsonObject = JSONObject.parseObject(result);
            return jsonObject.getString("access_token");
        } catch (Exception e) {
            log.error("获取token失败！", e);
            e.printStackTrace(System.err);
        }
        return null;
    }


    /**
     * 将一张本地图片转化成Base64字符串
     *
     * @param imgFile 图片地址
     * @return 图片转化base64后再UrlEncode结果
     */
    public static String getImageStrFromPath(File imgFile) {
        InputStream in;
        byte[] data = null;
        // 读取图片字节数组
        try {
            in = new FileInputStream(imgFile);
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (IOException e) {
            log.error("百度ocr识别错误", e);
            e.printStackTrace();
        }
        // 对字节数组Base64编码
        BASE64Encoder encoder = new BASE64Encoder();
        // 返回Base64编码过再URLEncode的字节数组字符串
        return encoder.encode(data);
    }

    //采用通用文字识别  调用量限制 50000次/天免费
    private static String POST_URL = "https://aip.baidubce.com/rest/2.0/ocr/v1/general_basic?access_token=";

    /**
     * 识别本地图片的文字
     *
     * @param path 本地图片地址
     * @return 识别结果，为json格式
     */
    public static String checkFile(String path) throws UnsupportedEncodingException {
        File file = new File(path);
        return checkFile(file);
    }

    /**
     * @param url 图片url
     * @return 识别结果，为json格式
     */
    public static String checkUrl(String url) {
        Map<String, Object> data = new HashMap<>();
        data.put("url", url);
        return HttpUtils.post(POST_URL + getAuth(), data);
    }


    public static String checkFile(File file) throws UnsupportedEncodingException {
        if (!file.exists()) {
            log.error("文件为空");
            throw new BaseException(BaseErrorEnums.ERROR_ARGS);
        }
        String image = getImageStrFromPath(file);
        Map<String, Object> param = new HashMap<>();
        param.put("image", image);
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        return HttpUtils.post(POST_URL + getAuth(), param, headers);
    }
}