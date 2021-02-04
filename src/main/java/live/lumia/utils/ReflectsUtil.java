package live.lumia.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Java Reflection Util
 *
 * @author zhulx
 */

public class ReflectsUtil {

    public static void beanSet(Object o, String pname, Class<?> c, Object v) throws Exception {
        String mn = "set" + pname.substring(0, 1).toUpperCase() + pname.substring(1);
        Class<?>[] acs = new Class<?>[1];
        Object[] os = new Object[1];
        acs[0] = c;
        os[0] = v;
        invokeMethod(o, mn, acs, os);
    }

    public static Object beanGet(Object o, String pname) throws Exception {
        String mn = "get" + pname.substring(0, 1).toUpperCase() + pname.substring(1);
        Class<?>[] acs = new Class<?>[0];
        Object[] os = new Object[0];
        return invokeMethod(o, mn, acs, os);
    }

    /**
     * 得到某个对象的公共属性
     *
     * @param owner , fieldName
     * @return 该属性对象
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws Exception
     */
    public static Object getProperty(Object owner, String fieldName) {
        try {
            Field field = owner.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(owner);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    /**
     * 得到一个VO类的属性名称集合
     *
     * @param clazz
     * @return
     */
    public static Map<String, Class<?>> getPropertyNames(Class<?> clazz) {
        Method[] methods = clazz.getMethods();
        Map<String, Class<?>> pl = new HashMap<>();
        for (Method method : methods) {
            String name = method.getName();
            if (name.startsWith("set") && name.length() > 3 && method.getParameterTypes().length == 1) {
                name = name.substring(3, 4).toLowerCase() + name.substring(4);
                pl.put(name, method.getParameterTypes()[0]);
            }
        }
        return pl;
    }


    /**
     * 得到某类的静态公共属性
     *
     * @param className 类名
     * @param fieldName 属性名
     * @return 该属性对象
     * @throws Exception
     */
    public static Object getStaticProperty(String className, String fieldName) throws Exception {
        Class<?> ownerClass = Class.forName(className);
        return ownerClass.getField(fieldName).get(ownerClass);

    }


    /**
     * 执行某对象方法
     *
     * @param owner      对象
     * @param methodName 方法名
     * @param args       参数
     * @return 方法返回值
     * @throws Exception
     */
    public static Object invokeMethod(Object owner, String methodName, Object[] args) throws Exception {
        Class<?>[] argsClass = (Class<?>[]) Stream.of(args).map(Object::getClass).toArray();
        return owner.getClass().getMethod(methodName, argsClass).invoke(owner, args);
    }

    /**
     * 执行某对象方法
     *
     * @param owner      对象
     * @param methodName 方法名
     * @param args       参数
     * @return 方法返回值
     * @throws Exception
     */
    public static Object invokeMethod(Object owner, String methodName, Class<?>[] argsClass, Object[] args) throws Exception {
        return owner.getClass().getMethod(methodName, argsClass).invoke(owner, args);
    }

    /**
     * 执行某类的静态方法
     *
     * @param className  类名
     * @param methodName 方法名
     * @param args       参数数组
     * @return 执行方法返回的结果
     * @throws Exception
     */
    public static Object invokeStaticMethod(String className, String methodName, Object[] args) throws Exception {
        Class<?>[] argsClass = new Class<?>[args.length];
        for (int i = 0, j = args.length; i < j; i++) {
            argsClass[i] = args[i].getClass();
        }
        return Class.forName(className).getMethod(methodName, argsClass).invoke(null, args);
    }

    /**
     * 新建实例
     *
     * @param newoneClass 类名
     * @param args        构造函数的参数
     * @return 新建的实例
     * @throws Exception
     */
    public static Object newInstance(Class<?> newoneClass, Object[] args) throws Exception {
        Class<?>[] argsClass = new Class<?>[args.length];
        for (int i = 0, j = args.length; i < j; i++) {
            argsClass[i] = args[i].getClass();
        }
        return newoneClass.getConstructor(argsClass).newInstance(args);
    }

    /**
     * 新建实例
     *
     * @param newoneClass 类名
     * @return 新建的实例
     * @throws Exception
     */
    public static Object newInstance(Class<?> newoneClass) throws Exception {
        return newoneClass.getConstructor(new Class<?>[0]).newInstance();
    }

    /**
     * 新建实例
     *
     * @param className 类名
     * @param args      构造函数的参数
     * @return 新建的实例
     * @throws Exception
     */
    public static Object newInstance(String className, Object[] args) throws Exception {
        return newInstance(Class.forName(className), args);

    }

    /**
     * 是不是某个类的实例
     *
     * @param obj 实例
     * @param cls 类
     * @return 如果 obj 是此类的实例，则返回 true
     */
    public static boolean isInstance(Object obj, Class<?> cls) {
        return cls.isInstance(obj);
    }

}
