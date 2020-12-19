package live.lumia.utils;

import java.math.BigDecimal;
import java.util.*;


/**
 * @author l5990
 */
public class ObjectUtil {
    public static Object[] nullFilter(Object... ps) {
        return Arrays.stream(ps).filter(Objects::nonNull).toArray();
    }


    public static <T> Set<T> array2Set(T[] args) {
        return new HashSet<T>(Arrays.asList(args));
    }


    /**
     * 取得整数值
     *
     * @param o
     * @return
     */
    static public int getInteger(Object o) {
        try {
            if (o instanceof Integer) {
                return (Integer) o;
            }
            if (o == null) {
                return 0;
            }
            return Integer.parseInt(o.toString());
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 取得整数值
     *
     * @param o
     * @return
     */
    static public short getShort(Object o) {
        try {
            if (o instanceof Short) {
                return (Short) o;
            }
            if (o == null) {
                return 0;
            }
            return Short.parseShort(o.toString());
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 取得字符串值
     *
     * @param o
     * @return
     */
    static public String getString(Object o) {
        try {
            if (o == null) {
                return "";
            } else {
                return o.toString().trim();
            }
        } catch (Exception e) {
            return "";
        }
    }

    static public Boolean getBoolean(Object o) {
        try {
            if (o == null) {
                return false;
            } else {
                if (o instanceof Boolean) {
                    return (Boolean) o;
                }
                String s = o.toString().toLowerCase().trim();
                return ("T".equals(s) || "1".equals(s) || "true".equals(s) || "yes".equals(s) || "on".equals(s) || "是".equals(s));
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 取得长整型值
     *
     * @param o
     * @return
     */
    static public long getLong(Object o) {
        try {
            if (o instanceof Long) {
                return (Long) o;
            }
            if (o == null) {
                return 0;
            }
            return Long.parseLong(o.toString());
        } catch (Exception e) {
            return 0;
        }
    }


    /**
     * 取得浮点数值
     *
     * @param o
     * @return
     */
    static public float getFloat(Object o) {
        try {
            if (o instanceof Float) {
                return (Float) o;
            }
            if (o == null) {
                return 0;
            }
            return Float.parseFloat(o.toString());
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 取得浮点数值
     *
     * @param o
     * @return
     */
    static public double getDouble(Object o) {
        try {
            if (o instanceof Double) {
                return (Double) o;
            }
            if (o == null) {
                return 0;
            }
            return Double.parseDouble(o.toString());
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 取得浮点数值
     *
     * @param o
     * @return
     */
    static public BigDecimal getBigDecimal(Object o) {
        try {
            if (o instanceof BigDecimal) {
                return (BigDecimal) o;
            }
            if (o == null) {
                return BigDecimal.ZERO;
            }
            return BigDecimal.valueOf(Double.parseDouble(o.toString()));
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * 取得浮点数值
     *
     * @param o
     * @return
     */
    static public float getFloat(Object o, int precision) {
        try {
            int p = (int) Math.pow(10, precision);
            int v = Math.round(Float.parseFloat(o.toString()) * p);
            return ((float) v) / p;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 将字符串转换成为日期类型
     *
     * @param o
     * @return
     */
    public static Date getDate(Object o) {
        try {
            String dateStr = o.toString();
            String[] d = dateStr.split("[^0-9]");
            int l = Math.min(6, d.length);
            Calendar date = Calendar.getInstance();
            int[] di = {0, 0, 0, 0, 0, 0};
            for (int i = 0; i < l; i++) {
                di[i] = Integer.parseInt(d[i]);
            }
            date.set(di[0], di[1] - 1, di[2], di[3], di[4], di[5]);
            date.set(Calendar.MILLISECOND, 0);
            return date.getTime();
        } catch (Exception e) {
            return null;
        }
    }

    static public Object getValue(Object val, Class clazz) {
        try {
            if (val.getClass().equals(clazz)) {
                return val;
            }
            if (clazz.equals(String.class)) {
                return getString(val);
            }
            if (clazz.equals(Long.class)) {
                return getLong(val);
            }
            if (clazz.equals(Boolean.class)) {
                return getBoolean(val);
            }
            if (clazz.equals(Date.class)) {
                return getDate(val);
            }
            if (clazz.equals(Double.class)) {
                return getDouble(val);
            }
            if (clazz.equals(BigDecimal.class)) {
                return getBigDecimal(val);
            }
            if (clazz.equals(Float.class)) {
                return getFloat(val);
            }
            if (clazz.equals(Short.class)) {
                return getShort(val);
            }
        } catch (Exception ignored) {

        }
        return val;
    }


    /**
     * 生成固定长度的字符串
     *
     * @param length
     * @return
     */
    public static String getRandomNum(int length) {
        String[] array = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0"};
        Random rand = new Random();
        for (int i = 10; i > 1; i--) {
            int index = rand.nextInt(i);
            String tmp = array[index];
            array[index] = array[i - 1];
            array[i - 1] = tmp;
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            result.append(array[i]);
        }
        return result.toString();
    }

}
