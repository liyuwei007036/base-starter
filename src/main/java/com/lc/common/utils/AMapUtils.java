package com.lc.common.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.util.StringUtils;

import java.net.URLDecoder;

/**
 * 地图工具
 */
public class AMapUtils {
    private AMapUtils() {
    }

    public volatile static AMapUtils mapUtils;

    public static AMapUtils getInstance() {
        if (mapUtils == null) {
            synchronized (AMapUtils.class) {
                if (mapUtils == null) {
                    mapUtils = new AMapUtils();
                }
            }
        }
        return mapUtils;
    }

    private static final String KEY = "46c4484db0f3903f1bc56ff151055f26";

    /**
     * 根据坐标地址返回经纬度
     *
     * @param address
     * @return
     */
    public String getCoordinateByAddress(String address) {
        if (StringUtils.isEmpty(address)) {
            return null;
        }

        String url = "http://restapi.amap.com/v3/geocode/geo?batch=true&address=" + address + "&output=JSON&key=" + KEY;

        JSONObject data = JSONObject.parseObject(HttpUtils.get(url));

        int status = data.getInteger("status");

        // 转换失败
        if (status == 0) return null;

        JSONArray o = data.getJSONArray("geocodes");

        return o.getJSONObject(0).getString("location");
    }

    /**
     * 根据坐标地址返回经纬度
     *
     * @param address
     * @return
     */
    public JSONArray getCoordinateByAddress(JSONArray address) {

        if (address.size() < 1) throw new RuntimeException("查询地址为空");

        if (address.size() > 10) throw new RuntimeException("最大查询地址10个");

        String ads = "";

        for (int i = 0; i < address.size(); i++) {
            ads += address.getString(i);
            if (i != address.size()) {
                ads += "|";
            }
        }

        String url = "http://restapi.amap.com/v3/geocode/geo?batch=true&address=" + ads + "&output=JSON&key=" + KEY;

        JSONObject data = JSONObject.parseObject(HttpUtils.get(url));

        int status = data.getInteger("status");

        // 转换失败
        if (status == 0) return null;

        JSONArray o = data.getJSONArray("geocodes");

        JSONArray array = new JSONArray();

        for (int i = 0; i < o.size(); i++) {
            array.add(o.getJSONObject(i).getString("location"));
        }
        return array;
    }


    /**
     * 通过经纬度获取距离(单位：米)
     *
     * @return
     */
    public double calculateLineDistance(String start, String end) {
        if ((start == null) || (end == null)) {
            throw new IllegalArgumentException("非法坐标值，不能为null");
        }
        double d1 = 0.01745329251994329D;
        double d2 = ObjectUtil.getDouble(start.split(",")[0]);
        double d3 = ObjectUtil.getDouble(start.split(",")[1]);
        double d4 = ObjectUtil.getDouble(end.split(",")[0]);
        double d5 = ObjectUtil.getDouble(end.split(",")[1]);
        d2 *= d1;
        d3 *= d1;
        d4 *= d1;
        d5 *= d1;
        double d6 = Math.sin(d2);
        double d7 = Math.sin(d3);
        double d8 = Math.cos(d2);
        double d9 = Math.cos(d3);
        double d10 = Math.sin(d4);
        double d11 = Math.sin(d5);
        double d12 = Math.cos(d4);
        double d13 = Math.cos(d5);
        double[] arrayOfDouble1 = new double[3];
        double[] arrayOfDouble2 = new double[3];
        arrayOfDouble1[0] = (d9 * d8);
        arrayOfDouble1[1] = (d9 * d6);
        arrayOfDouble1[2] = d7;
        arrayOfDouble2[0] = (d13 * d12);
        arrayOfDouble2[1] = (d13 * d10);
        arrayOfDouble2[2] = d11;
        double d14 = Math.sqrt((arrayOfDouble1[0] - arrayOfDouble2[0]) * (arrayOfDouble1[0] - arrayOfDouble2[0])
                + (arrayOfDouble1[1] - arrayOfDouble2[1]) * (arrayOfDouble1[1] - arrayOfDouble2[1])
                + (arrayOfDouble1[2] - arrayOfDouble2[2]) * (arrayOfDouble1[2] - arrayOfDouble2[2]));

        return (Math.asin(d14 / 2.0D) * 12742001.579854401D);
    }

    public void getDistanceFromAmap(String a, String b) {
        String url = "http://restapi.amap.com/v4/direction/bicycling?type=0&origin=" + a + "&destination=" + b + "&key=" + KEY;
        System.out.println(HttpUtils.get(url));
    }

    /**
     * 逆地理编码
     *
     * @param code
     * @return
     */
    public String regeocode(String code) {
        if (StringUtils.isEmpty(code)) return "";
        String url = "http://restapi.amap.com/v3/geocode/regeo?output=JSON&location=" + code + "&key=" + KEY + "&radius=1000&extensions=all";
        JSONObject o = JSONObject.parseObject(HttpUtils.get(url));
        if (o.getInteger("status") != 1) {
            return "";
        }
        return o.getJSONObject("regeocode").getString("formatted_address");

    }

    public JSONObject getAddressByIp(String ip) {
        String url = String.format("https://restapi.amap.com/v3/ip?ip=%s&output=json&key=%s", ip, "9719ff2496fd71883230596166678b9f");
        JSONObject amap_res = JSONObject.parseObject(HttpUtils.get(url));
        Integer status = amap_res.getInteger("status");
        String province = "未知", city = "未知";
        if (ObjectUtil.getInteger(status) != 1) {
            if (!StringUtils.isEmpty(amap_res.getString("province"))) {
                province = amap_res.getString("province");
            }
            if (!StringUtils.isEmpty(amap_res.getString("city"))) {
                city = amap_res.getString("city");
            }
        }
        amap_res.clear();
        amap_res.put("province", province);
        amap_res.put("city", city);
        return amap_res;
    }


    public String getCityFromIP(String ip) {
        try {
            String json = URLDecoder.decode(HttpUtils.get("http://api.map.baidu.com/location/ip?ip="
                    + ip + "&ak=Ufs5jopdEVSA07kGiXxAulFCAL9QaRsS"), "utf-8");
            JSONObject jsStr = JSONObject.parseObject(json); //将字符串{“id”：1}
            String a = jsStr.getString("address");
            String[] s = a.split("\\|");
            return PinyinUtils.getFullSpell(s[2]);
        } catch (Exception e) {
            e.printStackTrace();
            return "china";
        }
    }

    /**
     * 通过经纬度获取城市
     *
     * @param longitude
     * @param latitude
     * @return
     */
    public String getCityByLongitudeAndLatitude(String longitude, String latitude) {
        String url = "http://restapi.amap.com/v3/geocode/regeo?key=" + KEY + "&location=" + longitude + "," + latitude;
        String result = HttpUtils.get(url);
        JSONObject object = JSONObject.parseObject(result);
        JSONObject o = object.getJSONObject("regeocode");
        o = o.getJSONObject("addressComponent");
        String city = o.getString("city");
        String last = city.substring(city.length() - 1);
        if (last.equals("市")) {
            city = city.substring(0, city.length() - 1);
        }
        return city;
    }
}

