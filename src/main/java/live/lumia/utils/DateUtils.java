package live.lumia.utils;

import org.springframework.util.StringUtils;

import java.security.InvalidParameterException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

/**
 * @author zhulx
 * @date 2016/10/23
 */
public class DateUtils {

    private static final String YMDHMS = "yyyy-MM-dd HH:mm:ss";
    private static final String YMDHM = "yyyy-MM-dd HH:mm";
    private static final String YMD = "yyyy-MM-dd";


    private static SimpleDateFormat getDateFormat(String rule) {
        return new SimpleDateFormat(rule);
    }

    /**
     * 格式化日期
     *
     * @param date 时间
     * @param fmt  格式
     * @return str
     */
    public static String dateToStr(Date date, String fmt) {
        if (date != null) {
            SimpleDateFormat sdf = new SimpleDateFormat(fmt);
            return sdf.format(date);
        } else {
            return null;
        }
    }

    public static String dateToStr(Date date, String fmt, String defaultValue) {
        if (date != null) {
            SimpleDateFormat sdf = new SimpleDateFormat(fmt);
            return sdf.format(date);
        } else {
            return defaultValue;
        }
    }

    public static String dateToStr(Date date) {
        if (date != null) {
            return getDateFormat(YMD).format(date);
        } else {
            return null;
        }
    }

    public static String datetimeToStr(Date date) {
        if (date != null) {
            return getDateFormat(YMDHM).format(date);
        } else {
            return null;
        }
    }

    public static String now() {
        return getDateFormat(YMDHMS).format(new Date());
    }

    public static String dateString() {
        return getDateFormat(YMD).format(new Date());
    }

    public static String format(Date date) {
        if (null == date) {
            return null;
        }

        return getDateFormat(YMDHMS).format(date);
    }

    public static Date parseDateString(String str) {
        Date d = null;
        if (!StringUtils.isEmpty(str)) {
            int ymdLength = 10;
            int ymdhmsLength = 19;
            int ymdhmLength = 16;
            try {
                if (str.length() == ymdLength) {
                    d = getDateFormat(YMD).parse(str);
                } else if (str.length() == ymdhmsLength) {
                    d = getDateFormat(YMDHMS).parse(str);
                } else if (str.length() == ymdhmLength) {
                    d = getDateFormat(YMDHM).parse(str);
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
     * @param date 时间
     * @return 时间
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
        return null;
    }

    /**
     * 清除日期时间的毫秒为0
     *
     * @param date 时间
     * @return 时间
     */
    public static Date clearMilliTime(Date date) {
        if (date != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.set(Calendar.MILLISECOND, 0);
            return cal.getTime();
        }
        return null;
    }

    /**
     * 统计两个日期之间包含的天数。
     *
     * @param date1 1
     * @param date2 2
     * @return 1
     */
    public static int getDayDiff(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            throw new InvalidParameterException("date1 and date2 cannot be null!");
        }
        long millSecondsInOneDay = 24 * 60 * 60 * 1000;
        return (int) ((date1.getTime() - date2.getTime()) / millSecondsInOneDay);
    }


    /**
     * 获取指定月份的总天数
     *
     * @param month 1
     * @return 2
     */
    public static int getTotalDays(int year, int month) {
        int monthNum = 12;
        int yearBegin = 1970;
        if (month > monthNum || month <= 0 || year <= yearBegin) {
            return 0;
        }
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, 0);
        return cal.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 计算2个时间之间的秒数
     *
     * @param date1 1
     * @param date2 1
     * @return 1
     */
    public static int getDaysSeconds(Date date1, Date date2) {
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
     * @param nums 月数
     * @return 时间
     */
    public static Date getBeforeNowMonth(Integer nums) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -nums);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return clearTime(clearMilliTime(calendar.getTime()));
    }


    /**
     * LocalDate -> Date
     */
    public static Date asDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }


    /**
     * LocalDateTime -> Date
     *
     * @param localDateTime
     * @return
     */
    public static Date asDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Date -> LocalDate
     *
     * @param date
     * @return
     */
    public static LocalDate asLocalDate(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * Date -> LocalDateTime
     *
     * @param date
     * @return
     */
    public static LocalDateTime asLocalDateTime(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
