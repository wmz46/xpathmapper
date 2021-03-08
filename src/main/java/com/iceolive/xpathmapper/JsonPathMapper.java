package com.iceolive.xpathmapper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.alibaba.fastjson.annotation.JSONField;
import com.iceolive.util.StringUtil;
import com.iceolive.xpathmapper.annotation.JsonPath;
import com.iceolive.xpathmapper.util.ReflectUtil;
import lombok.Data;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author:wangmianzhe
 **/
public class JsonPathMapper {
    public static <T> T parse(String json, Class<T> clazz) {
        return parse(JSON.parse(json), clazz, null);
    }

    public static <T> T parse(Object obj, Class<T> clazz) {
        return parse(obj, clazz, null);
    }

    private static <T> T parse(Object obj, Class<T> clazz, String format) {
        if (ReflectUtil.isBasicType(clazz)) {
            if (obj != null) {
                return (T) StringUtil.parse(obj.toString(), format, clazz);
            } else {
                return null;
            }
        }
        List<Field> fields = ReflectUtil.getAllFields(clazz);
        T newObj = ReflectUtil.newInstance(clazz);
        for (Field field : fields) {
            JsonPath jsonPath = field.getAnnotation(JsonPath.class);
            if (jsonPath == null || StringUtil.isEmpty(jsonPath.value())) {
                continue;
            }
            JSONField jsonField = field.getAnnotation(JSONField.class);
            String dateFormat = "";
            if (jsonField != null && !StringUtil.isEmpty(jsonField.format())) {
                dateFormat = jsonField.format();
            }
            boolean isList = false;
            boolean isArray = false;
            boolean isSet = false;
            Class<?> type = null;
            if (field.getType().isArray()) {
                type = field.getType().getComponentType();
                isArray = true;
            } else if (field.getGenericType() instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
                if (List.class.isAssignableFrom((Class<?>) parameterizedType.getRawType())) {
                    type = (Class) parameterizedType.getActualTypeArguments()[0];
                    isList = true;
                } else if (Set.class.isAssignableFrom((Class<?>) parameterizedType.getRawType())) {
                    type = (Class) parameterizedType.getActualTypeArguments()[0];
                    isSet = true;
                }
            } else {
                type = field.getType();
            }
            if (isArray) {
                Object currentObj = JSONPath.eval(obj, jsonPath.value());
                if (currentObj instanceof JSONObject) {
                    Object values = ReflectUtil.newInstance(field, 1);
                    Object item = parse(currentObj, type, dateFormat);
                    Array.set(values, 0, item);
                    ReflectUtil.setValue(newObj, field.getName(), values);

                } else if (currentObj instanceof JSONArray) {
                    Object values = ReflectUtil.newInstance(field, ((JSONArray) currentObj).size());
                    for (int i = 0; i < ((JSONArray) currentObj).size(); i++) {
                        Object item = parse(((JSONArray) currentObj).get(i), type, dateFormat);
                        Array.set(values, i, item);
                    }
                    ReflectUtil.setValue(newObj, field.getName(), values);

                } else if (currentObj.getClass().isArray()) {
                    Object values = ReflectUtil.newInstance(field, Array.getLength(currentObj));
                    for (int i = 0; i < Array.getLength(currentObj); i++) {
                        Object item = parse(Array.get(currentObj, i), type, dateFormat);
                        Array.set(values, i, item);
                    }
                    ReflectUtil.setValue(newObj, field.getName(), values);

                } else {
                    Object values = ReflectUtil.newInstance(field, 1);
                    Object item = parse(currentObj, type, dateFormat);
                    Array.set(values, 0, item);
                    ReflectUtil.setValue(newObj, field.getName(), values);
                }
            } else if (isList) {
                Object currentObj = JSONPath.eval(obj, jsonPath.value());
                if (currentObj instanceof JSONObject) {
                    Object values = ReflectUtil.newInstance(field, 1);
                    Object item = parse(currentObj, type, dateFormat);
                    ((List) values).add(item);
                    ReflectUtil.setValue(newObj, field.getName(), values);

                } else if (currentObj instanceof JSONArray) {
                    Object values = ReflectUtil.newInstance(field, ((JSONArray) currentObj).size());
                    for (int i = 0; i < ((JSONArray) currentObj).size(); i++) {
                        Object item = parse(((JSONArray) currentObj).get(i), type, dateFormat);
                        ((List) values).add(item);
                    }
                    ReflectUtil.setValue(newObj, field.getName(), values);

                } else if (List.class.isAssignableFrom(currentObj.getClass())) {
                    Object values = ReflectUtil.newInstance(field, ((List) currentObj).size());
                    for (int i = 0; i < ((List) currentObj).size(); i++) {
                        Object item = parse(((List) currentObj).get(i), type, dateFormat);
                        ((List) values).add(item);
                    }
                    ReflectUtil.setValue(newObj, field.getName(), values);

                } else {
                    Object values = ReflectUtil.newInstance(field, 1);
                    Object item = parse(currentObj, type, dateFormat);
                    ((List) values).add(item);
                    ReflectUtil.setValue(newObj, field.getName(), values);
                }
            } else if (isSet) {
                Object currentObj = JSONPath.eval(obj, jsonPath.value());
                if (currentObj instanceof JSONObject) {
                    Object values = ReflectUtil.newInstance(field, 1);
                    Object item = parse(currentObj, type, dateFormat);
                    ((Set) values).add(item);
                    ReflectUtil.setValue(newObj, field.getName(), values);

                } else if (currentObj instanceof JSONArray) {
                    Object values = ReflectUtil.newInstance(field, ((JSONArray) currentObj).size());
                    for (int i = 0; i < ((JSONArray) currentObj).size(); i++) {
                        Object item = parse(((JSONArray) currentObj).get(i), type, dateFormat);
                        ((Set) values).add(item);
                    }
                    ReflectUtil.setValue(newObj, field.getName(), values);

                } else if (Set.class.isAssignableFrom(currentObj.getClass())) {
                    Object values = ReflectUtil.newInstance(field, ((Set) currentObj).size());
                    for (Object c : (Set) currentObj) {
                        Object item = parse(c, type, dateFormat);
                        ((Set) values).add(item);
                    }
                    ReflectUtil.setValue(newObj, field.getName(), values);

                } else {
                    Object values = ReflectUtil.newInstance(field, 1);
                    Object item = parse(currentObj, type, dateFormat);
                    ((Set) values).add(item);
                    ReflectUtil.setValue(newObj, field.getName(), values);
                }
            } else {
                Object currentObj = JSONPath.eval(obj, jsonPath.value());
                Object item = parse(currentObj, type, dateFormat);
                ReflectUtil.setValue(newObj, field.getName(), item);
            }
        }
        return newObj;
    }

}
