package live.lumia.utils;

import live.lumia.utils.qqway.IPZone;
import live.lumia.utils.qqway.QQWry;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * ipv4工具类
 *
 * @author liyuwei
 */
@Slf4j
public class IPV4Utils {

    private static QQWry qqWry;

    static {
        try {
            qqWry = new QQWry();
            log.debug("IP库版本：{}", qqWry.getDatabaseVersion());
        } catch (IOException e) {
            log.error("IP工具初始化失败", e);
        }
    }

    /**
     * 获取地理位置
     *
     * @param ip
     * @return
     */
    public static String getLocation(String ip) {
        IPZone ip1 = qqWry.findIP(ip);
        return ip1.getMainInfo().replaceAll("CZ88.NET", "");
    }

    /**
     * 获取地理位置和运营商
     *
     * @param ip ip地理位置
     * @return
     */
    public static String getLocationAndOperator(String ip) {
        IPZone ip1 = qqWry.findIP(ip);
        return ip1.toString().replaceAll("CZ88.NET", "");
    }

    /**
     * 获取运营商
     *
     * @param ip
     * @return
     */
    public static String getOperator(String ip) {
        IPZone ip1 = qqWry.findIP(ip);
        return ip1.getSubInfo().replaceAll("CZ88.NET", "");
    }

    /**
     * 获取地理位置
     *
     * @param host
     * @return
     */
    public static String getLocationByHost(String host) {
        try {
            String ip = InetAddress.getByName(host).getHostName();
            return getLocation(ip);
        } catch (UnknownHostException e) {
            log.error("根据host获取地理位置失败", e);
            return "";
        }

    }


    /**
     * 获取地理位置和运营商
     *
     * @param host
     * @return
     */
    public static String getLocationAndOperatorByHost(String host) {
        try {
            String ip = InetAddress.getByName(host).getHostName();
            return getLocationAndOperator(ip);
        } catch (UnknownHostException e) {
            log.error("根据host获取地理位置失败", e);
            return "";
        }
    }

    /**
     * 获取运营商
     *
     * @param host
     * @return
     */
    public static String getOperatorByHost(String host) {
        try {
            String ip = InetAddress.getByName(host).getHostName();
            return getOperator(ip);
        } catch (UnknownHostException e) {
            log.error("根据host获取地理位置失败", e);
            return "";
        }
    }
}
