package com.iceolive.xpathmapper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * JsonPath注解
 *
 * @author:wangmianzhe
 **/
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonPath {
    /**
     * jsonPath
     * @return
     */
    String value();
}
