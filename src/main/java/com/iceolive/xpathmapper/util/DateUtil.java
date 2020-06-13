package com.iceolive.xpathmapper.util;

import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @Description:日期工具类
 * @Author:钢翼
 * @Email:659240788@qq.com
 * @DateTime Create in 下午 5:09 2020/2/23/023
 **/
@Slf4j
public class DateUtil {
    public static String format(LocalDate date, String format) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(format);
        return dateTimeFormatter.format(date);
    }

    public static String format(LocalDateTime date, String format) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(format);
        return dateTimeFormatter.format(date);
    }

    public static String format(Date date, String format) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        return simpleDateFormat.format(date);
    }

    public static <T> T parse(String date, String format, Class<T> clazz) {
        if (clazz.equals(LocalDate.class)) {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(format);
            return (T) LocalDate.parse(date, dateTimeFormatter);
        } else if (clazz.equals(LocalDateTime.class)) {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(format);
            return (T) LocalDateTime.parse(date, dateTimeFormatter);
        } else if (clazz.equals(Date.class)) {
            try {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
                return (T) simpleDateFormat.parse(date);
            } catch (ParseException e) {
                log.error("日期转化异常", e);
                return null;
            }
        } else {
            return null;
        }
    }
}
