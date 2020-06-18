package com.iceolive.xpathmapper.util;

import com.iceolive.util.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 反射工具类
 *
 * @author:wangmianzhe
 **/
@Slf4j
public class ReflectUtil {

    public static <T> List<Field> getAllFields(Class<T> baseClazz) {
        Class clazz = baseClazz;
        List<Field> fieldList = new ArrayList<>();
        while (clazz != null) {
            fieldList.addAll(new ArrayList<>(Arrays.asList(clazz.getDeclaredFields())));
            clazz = clazz.getSuperclass();
        }
        return fieldList;
    }

    public static <T> void setValue(Object m, String fieldName, Object value) {
        if (m != null) {
            setValue(m, fieldName, value, m.getClass());
        }
    }

    public static <T> Field getField(String fieldName, Class<T> clazz) {
        return getAllFields(clazz).stream().filter(m -> m.getName().equals(fieldName)).findFirst().orElse(null);
    }

    /**
     * 设置值
     *
     * @param m         对象
     * @param fieldName 字段名
     * @param value     值
     * @param clazz     类型
     */
    public static void setValue(Object m, String fieldName, Object value, Class<?> clazz) {
        if (StringUtil.isEmpty(fieldName)) {
            return;
        }
        try {
            Field field = getField(fieldName, clazz);
            if (field != null) {
                field.setAccessible(true);
                field.set(m, value);
            } else {
                return;
            }

        } catch (Exception e) {

        }
    }

    public static <T> Object getValue(Object m, String fieldName) {
        if (m != null) {
            return getValue(m, fieldName, m.getClass());
        } else {
            return null;
        }
    }

    /**
     * 获取值
     *
     * @param m         对象
     * @param fieldName 字段名
     * @param clazz     类型
     * @return 值
     */
    public static Object getValue(Object m, String fieldName, Class<?> clazz) {
        try {
            Field field = getField(fieldName, clazz);
            if (field != null) {
                field.setAccessible(true);
                Object value = field.get(m);
                if (value != null) {
                    return value;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }

    }

    public static Object newInstance(Field field, int length) {
        if (field.getType().isArray()) {
            return newInstance(field.getType(), length);
        } else if (field.getGenericType() instanceof ParameterizedType) {
            return newInstance(field.getType());
        } else {
            return newInstance(field.getType());
        }
    }

    public static Object newInstance(Class<?> clazz, int length) {
        return Array.newInstance(clazz.getComponentType(), length);
    }

    public static <T> T newInstance(Class<T> clazz) {
        try {
            if (clazz.isInterface()) {
                if (List.class.isAssignableFrom(clazz)) {
                    return (T) new ArrayList();
                } else if (Map.class.isAssignableFrom(clazz)) {
                    return (T) new HashMap();
                } else if (Set.class.isAssignableFrom(clazz)) {
                    return (T) new HashSet<>();
                } else {
                    throw new RuntimeException("不支持类型");
                }
            } else if (clazz.isPrimitive()) {
                return null;
            } else if (clazz.isAssignableFrom(Integer.class)) {
                return null;
            } else if (clazz.isAssignableFrom(Short.class)) {
                return null;
            } else if (clazz.isAssignableFrom(Long.class)) {
                return null;
            } else if (clazz.isAssignableFrom(Float.class)) {
                return null;
            } else if (clazz.isAssignableFrom(Double.class)) {
                return null;
            } else if (clazz.isAssignableFrom(BigDecimal.class)) {
                return null;
            } else if (clazz.isAssignableFrom(LocalDateTime.class)) {
                return null;
            } else if (clazz.isAssignableFrom(LocalDate.class)) {
                return null;
            } else if (clazz.isAssignableFrom(Boolean.class)) {
                return null;
            } else {
                Constructor[] cons = clazz.getConstructors();
                if (Arrays.stream(cons).filter(m -> m.toString().endsWith("()")).count() == 0) {
                    throw new RuntimeException("类型[" + clazz.getTypeName() + "]没有无参构造函数,无法反序列化");
                }
                return clazz.newInstance();
            }
        } catch (IllegalAccessException | InstantiationException e) {
            log.error("反射创建对象异常", e);
            throw new RuntimeException(e);
        }
    }
}
