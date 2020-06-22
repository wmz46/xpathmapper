package com.iceolive.xpathmapper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.iceolive.util.StringUtil;
import com.iceolive.xpathmapper.annotation.JsonPath;
import com.iceolive.xpathmapper.util.ReflectUtil;
import lombok.Data;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Set;

/**
 * @author:wangmianzhe
 **/
public class JsonPathMapper {
    public static <T> T parse(Object obj, Class<T> clazz) {
        List<Field> fields = ReflectUtil.getAllFields(clazz);
        T newObj = ReflectUtil.newInstance(clazz);
        for (Field field : fields) {
            JsonPath jsonPath = field.getAnnotation(JsonPath.class);
            if (jsonPath == null || StringUtil.isEmpty(jsonPath.value())) {
                continue;
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
                    Object item = parse(currentObj, type);
                    Array.set(values, 0, item);
                    ReflectUtil.setValue(newObj, field.getName(), values);

                } else if (currentObj instanceof JSONArray) {
                    Object values = ReflectUtil.newInstance(field, ((JSONArray) currentObj).size());
                    for (int i = 0; i < ((JSONArray) currentObj).size(); i++) {
                        Object item = parse(((JSONArray) currentObj).get(i), type);
                        Array.set(values, i, item);
                    }
                    ReflectUtil.setValue(newObj, field.getName(), values);

                } else {
                    Object values = ReflectUtil.newInstance(field, 1);
                    Object item = parse(currentObj, type);
                    Array.set(values, 0, item);
                    ReflectUtil.setValue(newObj, field.getName(), values);
                }
            }else{
                Object currentObj = JSONPath.eval(obj, jsonPath.value());
                ReflectUtil.setValue(newObj, field.getName(), currentObj);
            }
        }
        return newObj;
    }

    @Data
    public static class A {
        @JsonPath("$.a")
        private int a;
        @JsonPath("$.b")
        private int[] b;
        @JsonPath("$.c")
        private C c;
        @JsonPath("$.d")
        private C[] d;
    }

    @Data
    public static class C {
        @JsonPath("$.e")
        private int e;
        @JsonPath("$.d")
        private int d;
    }

    public static void main(String[] args) {
        String json = "{a:1,b:2,c:{d:3,e:4},d:[{d:2,e:1},{d:21,e:12}]}";
        Object obj = JSON.parse(json);
        A a = JsonPathMapper.parse(obj, A.class);
        System.out.println(JSON.toJSONString(a));
    }
}
