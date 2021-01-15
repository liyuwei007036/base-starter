package live.lumia.utils;


import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Slf4j
public class RequestUtils {

    private static final String[] mobileAgents = {"iphone", "android", "phone", "mobile", "wap", "netfront", "java", "opera mobi",
            "opera mini", "ucweb", "windows ce", "symbian", "series", "webos", "sony", "blackberry", "dopod", "nokia",
            "samsung", "palmsource", "xda", "pieplus", "meizu", "midp", "cldc", "motorola", "foma", "docomo",
            "up.browser", "up.link", "blazer", "helio", "hosin", "huawei", "novarra", "coolpad", "webos", "techfaith",
            "palmsource", "alcatel", "amoi", "ktouch", "nexian", "ericsson", "philips", "sagem", "wellcom", "bunjalloo",
            "maui", "smartphone", "iemobile", "spice", "bird", "zte-", "longcos", "pantech", "gionee", "portalmmm",
            "jig browser", "hiptop", "benq", "haier", "^lct", "320x320", "240x320", "176x220", "w3c ", "acs-", "alav",
            "alca", "amoi", "audi", "avan", "benq", "bird", "blac", "blaz", "brew", "cell", "cldc", "cmd-", "dang",
            "doco", "eric", "hipt", "inno", "ipaq", "java", "jigs", "kddi", "keji", "leno", "lg-c", "lg-d", "lg-g",
            "lge-", "maui", "maxo", "midp", "mits", "mmef", "mobi", "mot-", "moto", "mwbp", "nec-", "newt", "noki",
            "oper", "palm", "pana", "pant", "phil", "play", "port", "prox", "qwap", "sage", "sams", "sany", "sch-",
            "sec-", "send", "seri", "sgh-", "shar", "sie-", "siem", "smal", "smar", "sony", "sph-", "symb", "t-mo",
            "teli", "tim-", "tsm-", "upg1", "upsi", "vk-v", "voda", "wap-", "wapa", "wapi", "wapp", "wapr", "webc",
            "winw", "winw", "xda", "xda-", "googlebot-mobile"};

    public static boolean isAjaxRequest(HttpServletRequest request) {
        String header = request.getHeader("X-Requested-With");
        return "XMLHttpRequest".equalsIgnoreCase(header);
    }

    public static boolean isMultipartRequest(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase().contains("multipart");
    }

    /**
     * 是否是手机浏览器
     *
     * @return
     */
    public static boolean isMobileBrowser(HttpServletRequest request) {
        String ua = request.getHeader(HttpHeaders.USER_AGENT);
        if (ua == null) {
            return false;
        }
        ua = ua.toLowerCase();
        for (String mobileAgent : mobileAgents) {
            if (ua.contains(mobileAgent)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否是微信浏览器
     *
     * @return
     */
    public static boolean isWechatBrowser(HttpServletRequest request) {
        String ua = request.getHeader(HttpHeaders.USER_AGENT);
        if (ua == null) {
            return false;
        }
        ua = ua.toLowerCase();
        return ua.indexOf("micromessenger") > 0;
    }

    /**
     * 是否是IE浏览器
     *
     * @return
     */
    public static boolean isIEBrowser(HttpServletRequest request) {
        String ua = request.getHeader(HttpHeaders.USER_AGENT);
        if (ua == null) {
            return false;
        }

        ua = ua.toLowerCase();
        if (ua.indexOf("msie") > 0) {
            return true;
        }
        return ua.indexOf("gecko") > 0 && ua.indexOf("rv:11") > 0;
    }

    public static String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-requested-For");
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Forwarded-For");
        }
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    public static String getUserAgent(HttpServletRequest request) {
        return request.getHeader(HttpHeaders.USER_AGENT);
    }


    public static String readData(HttpServletRequest request) {
        BufferedReader br = null;
        try {
            br = request.getReader();
            String line = br.readLine();
            String var4;
            if (line == null) {
                var4 = "";
            } else {
                StringBuilder ret = new StringBuilder();
                ret.append(line);

                while ((line = br.readLine()) != null) {
                    ret.append('\n').append(line);
                }
                var4 = ret.toString();
            }
            return var4;
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
        try (ByteArrayOutputStream sout = new ByteArrayOutputStream()) {
            int b;
            while ((b = ris.read()) != -1) {
                sout.write(b);
            }
            String sOk = sout.toString(StandardCharsets.UTF_8.name());
            return JSONObject.parseObject(sOk);
        }
    }
}
