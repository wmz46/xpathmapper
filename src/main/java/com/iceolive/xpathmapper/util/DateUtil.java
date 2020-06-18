package com.iceolive.xpathmapper.util;

import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 日期工具类
 * @author:wangmianzhe
 **/
@Slf4j
public class DateUtil {


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
