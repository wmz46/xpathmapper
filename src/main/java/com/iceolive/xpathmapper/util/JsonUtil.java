package com.iceolive.xpathmapper.util;

import com.jayway.jsonpath.JsonPath;


public class JsonUtil {
    public static Object parse(String json) {
        return JsonPath.parse(json).json();

    }
    public static String toJSONString(Object object) {
        return JsonPath.parse(object).jsonString();
    }

    public static Object eval(Object object,String jsonPath){
        try {
            Object result = JsonPath.parse(object).read(jsonPath);
            return result;
        }catch (RuntimeException e){
            e.printStackTrace();
            return null;
        }
    }
}
