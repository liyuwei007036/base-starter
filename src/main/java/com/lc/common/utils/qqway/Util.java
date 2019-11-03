package com.lc.common.utils.qqway;


import lombok.extern.log4j.Log4j2;

import java.io.UnsupportedEncodingException;
import java.util.StringTokenizer;

@Log4j2
public class Util {
    private static StringBuilder sb = new StringBuilder();

    public static byte[] getIpByteArrayFromString(String ip) {
        byte[] ret = new byte[4];
        if (ip == null) {
            return null;
        }
        if (ip.indexOf(':') > -1) {
            if ("0:0:0:0:0:0:0:1".equals(ip)) {
                ret[0] = 127;
                ret[1] = 0;
                ret[2] = 0;
                ret[3] = 1;
                return ret;
            } else {
                log.warn("IP" + ip + "可能是IPV6，无法解析。");
                return null;
            }
        }
        StringTokenizer st = new StringTokenizer(ip, ".");
        try {
            ret[0] = (byte) (Integer.parseInt(st.nextToken()) & 0xFF);
            ret[1] = (byte) (Integer.parseInt(st.nextToken()) & 0xFF);
            ret[2] = (byte) (Integer.parseInt(st.nextToken()) & 0xFF);
            ret[3] = (byte) (Integer.parseInt(st.nextToken()) & 0xFF);
        } catch (Exception e) {
            //log.error("从ip的字符串形式得到字节数组形式报错" + e);
            return null;
        }
        return ret;
    }

    public static String getIpStringFromBytes(byte[] ip) {
        sb = new StringBuilder("");
//	    sb.delete(0, sb.length());
        sb.append(ip[0] & 0xFF);
        sb.append('.');
        sb.append(ip[1] & 0xFF);
        sb.append('.');
        sb.append(ip[2] & 0xFF);
        sb.append('.');
        sb.append(ip[3] & 0xFF);
        return sb.toString();
    }

    public static String getString(byte[] b, int offset, int len, String encoding) {
        try {
            return new String(b, offset, len, encoding);
        } catch (UnsupportedEncodingException e) {
            return new String(b, offset, len);
        }
    }
}