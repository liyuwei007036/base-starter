package com.lc.core.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * json web token
 */
@Slf4j
public class JWTUtils {

    public static Boolean validSign(String mD5KEY, Map<Object, Object> parameters) {
        SortedMap<Object, Object> sortedMap = new TreeMap<>(parameters);
        StringBuffer sbkey = new StringBuffer();
        String signData = "";
        Long timestamp = 0L;
        Map<Object, Object> data = new HashMap<>();

        for (Map.Entry<Object, Object> m : sortedMap.entrySet()) {
            String k = ObjectUtil.getString(m.getKey());
            Object v = m.getValue();
            //空值不传递，不参与签名组串
            if (k.equals("sign")) {
                signData = ObjectUtil.getString(v);
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
                    sbkey.append(k).append("=").append(v).append("&");
                } else {
                    sbkey.append(k).append("=").append(v);
                }
            }
        }
        log.debug(sbkey.toString());

        Long s = System.currentTimeMillis() / 1000 + 3;
        if (s - timestamp > 60 * 1000 || timestamp <= 0 || s < timestamp) {
            log.debug("签名验证失败, 时间错误");
            return false;
        }
        String newSbkey = sbkey.toString() + mD5KEY;
        String sign = DigestUtils.md5DigestAsHex(newSbkey.getBytes()).toUpperCase();
        if (signData.equals(sign)) {
            log.debug("签名验证,成功");
            return true;
        } else {
            log.debug(signData);
            log.debug("-------------------------------------");
            log.debug(sign);
            log.debug("签名验证失败, Sign 值不匹配");
            return false;
        }
    }
}