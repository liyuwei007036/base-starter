package com.lc.common.utils;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import lombok.extern.log4j.Log4j2;
import net.minidev.json.JSONObject;
import org.springframework.util.DigestUtils;

import java.text.ParseException;
import java.util.*;

/**
 * json web token
 */
@Log4j2
public class JWTUtils {
    /**
     * 创建token　默认token 有效期２小时
     *
     * @param data   token携带的数据
     * @param secret 　token 加密秘钥
     * @return
     */
    public static JSONObject createToken(Object data, String secret) {
        JSONObject object = new JSONObject();
        try {
            Map<String, Object> payloadMap = new HashMap<>();
            Calendar c = Calendar.getInstance();

            //生成时间
            payloadMap.put("create_date", c.getTimeInMillis());
            payloadMap.put("data", data);

            //过期时间
            c.add(Calendar.HOUR_OF_DAY, 2);
            payloadMap.put("exp_date", c.getTimeInMillis());

            //3.建立一个头部Header
            /**
             * JWSHeader参数：1.加密算法法则,2.类型，3.。。。。。。。
             * 一般只需要传入加密算法法则就可以。
             * 这里则采用HS256
             *
             * JWSAlgorithm类里面有所有的加密算法法则，直接调用。
             */
            JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS256);

            //建立一个载荷Payload
            Payload payload = new Payload(new JSONObject(payloadMap));

            //将头部和载荷结合在一起
            JWSObject jwsObject = new JWSObject(jwsHeader, payload);

            //建立一个密匙
            JWSSigner jwsSigner = new MACSigner(secret.getBytes());

            //签名
            jwsObject.sign(jwsSigner);
            String token = jwsObject.serialize();
            object.put("token", token);
            object.put("expires", c.getTimeInMillis() - new Date().getTime());
            object.put("is_error", false);
        } catch (Exception e) {
            e.printStackTrace();
            object.put("msg", e.getMessage());
            object.put("is_error", true);
        } finally {
            return object;
        }
    }

    /**
     * 解析token 并验证是否过期
     *
     * @param token
     * @param secret
     * @return
     * @throws ParseException
     * @throws JOSEException
     */
    public static Map<String, Object> valid(String token, String secret) throws ParseException, JOSEException {
//        解析token
        JWSObject jwsObject = JWSObject.parse(token);

        //获取到载荷
        Payload payload = jwsObject.getPayload();

        JWSVerifier jwsVerifier = new MACVerifier(secret.getBytes());

        Map<String, Object> resultMap = new HashMap<>();

        //判断token是否合法
        if (jwsObject.verify(jwsVerifier)) {
            resultMap.put("valid", 0);
            //载荷的数据解析成json对象。
            JSONObject jsonObject = payload.toJSONObject();
            resultMap.put("data", jsonObject.get("data"));
            //判断token是否过期
            if (jsonObject.containsKey("exp_date")) {
                Long expTime = Long.valueOf(jsonObject.get("exp_date").toString());
                Long nowTime = new Date().getTime();
                //判断是否过期
                if (nowTime > expTime) {
                    //已经过期
                    resultMap.put("valid", 2);
                }
            } else {
                resultMap.put("valid", 1);
            }
        } else {
            resultMap.put("valid", 1);
        }
        return resultMap;
    }

    public static Boolean validSign(String MD5KEY, Map<Object, Object> parameters) {
        SortedMap<Object, Object> sortedMap = new TreeMap<>(parameters);
        StringBuffer sbkey = new StringBuffer();
        String sign_data = "";
        Long timestamp = 0L;
        Map<Object, Object> data = new HashMap<>();

        for (Map.Entry<Object, Object> m : sortedMap.entrySet()) {
            String k = ObjectUtil.getString(m.getKey());
            Object v = m.getValue();
            //空值不传递，不参与签名组串
            if (k.equals("sign")) {
                sign_data = ObjectUtil.getString(v);
                continue;
            }
            if (k.equals("timestamp")) {
                timestamp = ObjectUtil.getLong(v);
            }
            if (null != v && !"".equals(v)) {
                data.put(k, v);
            }
        }

        sortedMap = new TreeMap<>(data);

        int i = 0;
        for (Map.Entry<Object, Object> m : sortedMap.entrySet()) {
            i++;
            String k = ObjectUtil.getString(m.getKey());
            Object v = m.getValue();
            if (null != v && !"".equals(v)) {
                if (i != sortedMap.size()) {
                    sbkey.append(k + "=" + v + "&");
                } else {
                    sbkey.append(k + "=" + v);
                }
            }
        }
        log.debug(sbkey.toString());

        Long s = System.currentTimeMillis() / 1000 + 3;
        if (s - timestamp > 60 * 1000 || timestamp <= 0 || s < timestamp) {
            log.debug("签名验证失败, 时间错误");
            return false;
        }
        String new_sbkey = sbkey.toString() + MD5KEY;
        String sign = DigestUtils.md5DigestAsHex(new_sbkey.getBytes()).toUpperCase();
        if (sign_data.equals(sign)) {
            log.debug("签名验证,成功");
            return true;
        } else {
            log.debug(sign_data);
            log.debug("-------------------------------------");
            log.debug(sign);
            log.debug("签名验证失败, Sign 值不匹配");
            return false;
        }
    }
}