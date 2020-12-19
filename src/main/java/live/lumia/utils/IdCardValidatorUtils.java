package live.lumia.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 身份证号码校验工具
 *
 * @author l5990
 */
public class IdCardValidatorUtils {
    /**
     * <pre>
     * 省、直辖市代码表：
     *     11 : 北京  12 : 天津  13 : 河北       14 : 山西  15 : 内蒙古
     *     21 : 辽宁  22 : 吉林  23 : 黑龙江  31 : 上海  32 : 江苏
     *     33 : 浙江  34 : 安徽  35 : 福建       36 : 江西  37 : 山东
     *     41 : 河南  42 : 湖北  43 : 湖南       44 : 广东  45 : 广西      46 : 海南
     *     50 : 重庆  51 : 四川  52 : 贵州       53 : 云南  54 : 西藏
     *     61 : 陕西  62 : 甘肃  63 : 青海       64 : 宁夏  65 : 新疆
     *     71 : 台湾
     *     81 : 香港  82 : 澳门
     *     91 : 国外
     * </pre>
     */
    private static final String[] CITY_CODE = {"11", "12", "13", "14", "15", "21",
            "22", "23", "31", "32", "33", "34", "35", "36", "37", "41", "42",
            "43", "44", "45", "46", "50", "51", "52", "53", "54", "61", "62",
            "63", "64", "65", "71", "81", "82", "91"};

    /**
     * 每位加权因子
     */
    private static final int[] POWER = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};


    /**
     * 验证所有的身份证的合法性
     *
     * @param idCard 身份证
     * @return 合法返回true，否则返回false
     */
    public static boolean isValidatedAllIdCard(String idCard) {
        if (idCard == null || "".equals(idCard)) {
            return false;
        }
        if (idCard.length() == 15) {
            return validate15idCard(idCard);
        }
        return validate18idCard(idCard);
    }

    /**
     * <p>
     * 判断18位身份证的合法性
     * </p>
     * 根据〖中华人民共和国国家标准GB11643-1999〗中有关公民身份号码的规定，公民身份号码是特征组合码，由十七位数字本体码和一位数字校验码组成。
     * 排列顺序从左至右依次为：六位数字地址码，八位数字出生日期码，三位数字顺序码和一位数字校验码。
     * <p>
     * 顺序码: 表示在同一地址码所标识的区域范围内，对同年、同月、同 日出生的人编定的顺序号，顺序码的奇数分配给男性，偶数分配 给女性。
     * </p>
     * <p>
     * 1.前1、2位数字表示：所在省份的代码； 2.第3、4位数字表示：所在城市的代码； 3.第5、6位数字表示：所在区县的代码；
     * 4.第7~14位数字表示：出生年、月、日； 5.第15、16位数字表示：所在地的派出所的代码；
     * 6.第17位数字表示性别：奇数表示男性，偶数表示女性；
     * 7.第18位数字是校检码：也有的说是个人信息码，一般是随计算机的随机产生，用来检验身份证的正确性。校检码可以是0~9的数字，有时也用x表示。
     * </p>
     * <p>
     * 第十八位数字(校验码)的计算方法为： 1.将前面的身份证号码17位数分别乘以不同的系数。从第一位到第十七位的系数分别为：7 9 10 5 8 4
     * 2 1 6 3 7 9 10 5 8 4 2
     * </p>
     * <p>
     * 2.将这17位数字和系数相乘的结果相加。
     * </p>
     * <p>
     * 3.用加出来和除以11，看余数是多少
     * </p>
     * 4.余数只可能有0 1 2 3 4 5 6 7 8 9 10这11个数字。其分别对应的最后一位身份证的号码为1 0 X 9 8 7 6 5 4 3
     * 2。
     * <p>
     * 5.通过上面得知如果余数是2，就会在身份证的第18位数字上出现罗马数字的Ⅹ。如果余数是10，身份证的最后一位号码就是2。
     * </p>
     *
     * @param idCard
     * @return
     */
    public static boolean validate18idCard(String idCard) {
        if (idCard == null) {
            return false;
        }

        // 非18位为假
        if (idCard.length() != 18) {
            return false;
        }
        // 获取前17位
        String idCard17 = idCard.substring(0, 17);

        // 前17位全部为数字
        if (!isDigital(idCard17)) {
            return false;
        }

        String provinceId = idCard.substring(0, 2);
        // 校验省份
        if (!checkprovinceId(provinceId)) {
            return false;
        }

        // 校验出生日期
        String birthday = idCard.substring(6, 14);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

        try {
            Date birthDate = sdf.parse(birthday);
            String tmpDate = sdf.format(birthDate);
            // 出生年月日不正确
            if (!tmpDate.equals(birthday)) {
                return false;
            }

        } catch (ParseException e1) {

            return false;
        }

        // 获取第18位
        String idCard18Code = idCard.substring(17, 18);

        char[] c = idCard17.toCharArray();

        Integer[] bit = converCharToInt(c);

        int sum17 = 0;

        sum17 = getPowerSum(bit);

        // 将和值与11取模得到余数进行校验码判断
        String checkCode = getCheckCodeBySum(sum17);
        if (null == checkCode) {
            return false;
        }
        // 将身份证的第18位与算出来的校码进行匹配，不相等就为假
        return idCard18Code.equalsIgnoreCase(checkCode);
    }

    /**
     * 校验15位身份证
     * <p>
     * <pre>
     * 只校验省份和出生年月日
     * </pre>
     *
     * @param idCard
     * @return
     */
    public static boolean validate15idCard(String idCard) {
        if (idCard == null) {
            return false;
        }
        // 非15位为假
        if (idCard.length() != 15) {
            return false;
        }

        // 15全部为数字
        if (!isDigital(idCard)) {
            return false;
        }

        String provinceId = idCard.substring(0, 2);
        // 校验省份
        if (!checkprovinceId(provinceId)) {
            return false;
        }

        String birthday = idCard.substring(6, 12);

        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");

        try {
            Date birthDate = sdf.parse(birthday);
            String tmpDate = sdf.format(birthDate);
            // 身份证日期错误
            if (!tmpDate.equals(birthday)) {
                return false;
            }

        } catch (ParseException e1) {

            return false;
        }

        return true;
    }

    /**
     * 将15位的身份证转成18位身份证
     *
     * @param idCard
     * @return
     */
    public static String convertIdCarBy15bit(String idCard) {
        if (idCard == null) {
            return null;
        }

        // 非15位身份证
        int max = 15;
        if (idCard.length() != max) {
            return null;
        }

        // 15全部为数字
        if (!isDigital(idCard)) {
            return null;
        }

        String provinceId = idCard.substring(0, 2);
        // 校验省份
        if (!checkprovinceId(provinceId)) {
            return null;
        }

        String birthday = idCard.substring(6, 12);

        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");

        Date birthDate;
        try {
            birthDate = sdf.parse(birthday);
            String tmpDate = sdf.format(birthDate);
            // 身份证日期错误
            if (!tmpDate.equals(birthday)) {
                return null;
            }

        } catch (ParseException e1) {
            return null;
        }

        Calendar cDay = Calendar.getInstance();
        cDay.setTime(birthDate);
        String year = String.valueOf(cDay.get(Calendar.YEAR));

        String idCard17 = idCard.substring(0, 6) + year + idCard.substring(8);

        char[] c = idCard17.toCharArray();
        String checkCode = "";

        // 将字符数组转为整型数组
        Integer[] bit = converCharToInt(c);

        int sum17 = 0;
        sum17 = getPowerSum(bit);

        // 获取和值与11取模得到余数进行校验码
        checkCode = getCheckCodeBySum(sum17);

        // 获取不到校验位
        if (null == checkCode) {
            return null;
        }
        // 将前17位与第18位校验码拼接
        idCard17 += checkCode;
        return idCard17;
    }

    /**
     * 校验省份
     *
     * @param provinceId
     * @return 合法返回TRUE，否则返回FALSE
     */
    private static boolean checkprovinceId(String provinceId) {
        for (String id : CITY_CODE) {
            if (id.equals(provinceId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 数字验证
     *
     * @param str
     * @return
     */
    private static boolean isDigital(String str) {
        return str.matches("^[0-9]*$");
    }

    /**
     * 将身份证的每位和对应位的加权因子相乘之后，再得到和值
     *
     * @param bit
     * @return
     */
    private static int getPowerSum(Integer[] bit) {
        int sum = 0;
        if (POWER.length != bit.length) {
            return sum;
        }
        for (int i = 0; i < bit.length; i++) {
            for (int j = 0; j < POWER.length; j++) {
                if (i == j) {
                    sum = sum + bit[i] * POWER[j];
                }
            }
        }
        return sum;
    }

    /**
     * 将和值与11取模得到余数进行校验码判断
     *
     * @param sum17
     * @return 校验位
     */
    private static String getCheckCodeBySum(int sum17) {
        String checkCode = null;
        switch (sum17 % 11) {
            case 10:
                checkCode = "2";
                break;
            case 9:
                checkCode = "3";
                break;
            case 8:
                checkCode = "4";
                break;
            case 7:
                checkCode = "5";
                break;
            case 6:
                checkCode = "6";
                break;
            case 5:
                checkCode = "7";
                break;
            case 4:
                checkCode = "8";
                break;
            case 3:
                checkCode = "9";
                break;
            case 2:
                checkCode = "x";
                break;
            case 1:
                checkCode = "0";
                break;
            case 0:
                checkCode = "1";
                break;
            default:
                break;
        }
        return checkCode;
    }

    /**
     * 将字符数组转为整型数组
     *
     * @param c
     * @return
     * @throws NumberFormatException
     */
    private static Integer[] converCharToInt(char[] c) throws NumberFormatException {
        return Stream.of(c)
                .map(x -> Integer.parseInt(String.valueOf(x)))
                .collect(Collectors.toList())
                .toArray(new Integer[c.length]);

    }
}
