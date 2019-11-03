package com.lc.common.utils;

import java.security.InvalidParameterException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by zhulx on 2016/10/23.
 */
public class DateUtils {

    static SimpleDateFormat YMDHMS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    static SimpleDateFormat YMDHM = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    static SimpleDateFormat YMD = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 格式化日期
     *
     * @param date
     * @param fmt
     * @return
     */
    public static String dateToStr(Date date, String fmt) {
        if (date != null) {
            SimpleDateFormat sdf = new SimpleDateFormat(fmt);
            return sdf.format(date);
        } else {
            return null;
        }
    }

    public static String dateToStr(Date date, String fmt, String default_value) {
        if (date != null) {
            SimpleDateFormat sdf = new SimpleDateFormat(fmt);
            return sdf.format(date);
        } else {
            return default_value;
        }
    }

    public static String dateToStr(Date date) {
        if (date != null) {
            return YMD.format(date);
        } else {
            return null;
        }
    }

    public static String datetimeToStr(Date date) {
        if (date != null) {
            return YMDHM.format(date);
        } else {
            return null;
        }
    }

    public static String datetimesToStr(Date date) {
        if (date != null) {
            return YMDHMS.format(date);
        } else {
            return null;
        }
    }

    public static String now() {
        return YMDHMS.format(new Date());
    }

    public static String dateString() {
        return YMD.format(new Date());
    }

    public static String format(Date date) {
        if (null == date)
            return null;

        return YMDHMS.format(date);
    }

    public static Date parseDateString(String str) {
        Date d = null;
        if (str != null) {
            try {
                if (str.length() == 10) {
                    d = YMD.parse(str);
                } else if (str.length() == 19) {
                    d = YMDHMS.parse(str);
                } else if (str.length() == 16) {
                    d = YMDHM.parse(str);
                }
            } catch (ParseException e) {
                throw new RuntimeException(e.getMessage());
            }
        }
        return d;
    }

    public static int getYear() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.YEAR);
    }

    public static int getMonth() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.MONTH) + 1;
    }

    public static int getDay() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 清除日期时间的时分秒为0
     *
     * @param date
     * @return
     */
    public static Date clearTime(Date date) {
        if (date != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return cal.getTime();
        }
        return date;
    }

    /**
     * 清除日期时间的毫秒为0
     *
     * @param date
     * @return
     */
    public static Date clearMilliTime(Date date) {
        if (date != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.set(Calendar.MILLISECOND, 0);
            return cal.getTime();
        }
        return date;
    }

    /**
     * 统计两个日期之间包含的天数。
     *
     * @param date1
     * @param date2
     * @return
     */
    public static int getDayDiff(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            throw new InvalidParameterException("date1 and date2 cannot be null!");
        }
        long millSecondsInOneDay = 24 * 60 * 60 * 1000;
        return (int) ((date1.getTime() - date2.getTime()) / millSecondsInOneDay);
    }

    /**
     * 获取该季度起始月
     *
     * @param month
     * @return
     */
    public static int quarterFrom(int month) {
        if (month <= 3) {
            return 0;
        } else if (month == 4 || month == 5 || month == 6) {
            return 3;
        } else if (month == 7 || month == 8 || month == 9) {
            return 6;
        } else if (month == 10 || month == 11 || month == 12) {
            return 9;
        } else {
            return 0;
        }
    }

    /**
     * 获取该季度终止月
     *
     * @param month
     * @return
     */
    public static int quarterTo(int month) {
        if (month <= 3) {
            return 3;
        } else if (month == 4 || month == 5 || month == 6) {
            return 6;
        } else if (month == 7 || month == 8 || month == 9) {
            return 9;
        } else if (month == 10 || month == 11 || month == 12) {
            return 12;
        } else {
            return 3;
        }
    }

    /**
     * 获取指定月份的总天数
     *
     * @param month
     * @return
     */
    public static int getTotalDays(int year, int month) {
        if (month > 12 || month <= 0 || year <= 1970) return 0;
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, 0);
        return cal.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 计算2个时间之间的秒数
     *
     * @param date1
     * @param date2
     * @return
     */
    public static int getDaySiff(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            throw new InvalidParameterException("date1 and date2 cannot be null!");
        }
        date1 = clearMilliTime(date1);
        date2 = clearMilliTime(date2);
        long millSecondsInOneDay = 1000;
        return (int) ((date1.getTime() - date2.getTime()) / millSecondsInOneDay);
    }

    /**
     * 将一个时间往前推多少个月
     *
     * @param nums
     * @return
     */
    public static Date getBeforeNowMonth(Integer nums) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -nums);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return clearTime(clearMilliTime(calendar.getTime()));
    }
}
