package com.iceolive.xpathmapper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * xPath注解
 *
 * @author:wangmianzhe
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface XPath {
    /**
     * 必须是完整路径的xpath。如果是嵌套对象，则可以使用相对路径
     * /text() 结尾表示取节点内文本值
     * /@xxx 结尾表示取xxx的属性值
     *
     * @return xpath
     */
    String value();

    /**
     * @return 日期格式，类型为Date,LocalDate,LocalDateTime时生效
     */
    String format() default "";

    /**
     * @return  是否嵌套CDATA节点
     */
    boolean CDATA() default false;

    String trueString() default "true";

    String falseString() default "false";
}
