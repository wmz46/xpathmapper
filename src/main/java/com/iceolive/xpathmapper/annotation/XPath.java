package com.iceolive.xpathmapper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Description: xPath注解
 * @Author:钢翼
 * @Email:659240788@qq.com
 * @DateTime Create in 下午 9:56 2020/6/13/013
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface XPath {
    /**
     * 必须是完整路径的xpath。如果是嵌套对象，则可以使用相对路径
     * /text() 结尾表示取节点内文本值
     * /@xxx 结尾表示取xxx的属性值
     * @return
     */
    String value();

    /**
     * 日期格式，类型为Date,LocalDate,LocalDateTime时生效
     * @return
     */
    String format() default "";
}
