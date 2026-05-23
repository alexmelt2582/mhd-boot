package com.mhd.boot.common.utils.date;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * 时间工具类
 *
 * @author zhao-hao-dong
 **/
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateUtils {
    public static final String STANDARD_PATTERN_8 = "yyyyMMdd";
    public static final String STANDARD_PATTERN_10 = "yyyy-MM-dd";
    public static final String STANDARD_PATTERN_14 = "yyyyMMddHHmmss";
    public static final String STANDARD_PATTERN_15 = "yyyyMMddHHmmssS";
    public static final String STANDARD_PATTERN_16 = "yyyy-MM-dd HH:mm";
    public static final String STANDARD_PATTERN_19 = "yyyy-MM-dd HH:mm:ss";

    /**
     * 当前时间转换为时间字符串，默认格式为 "yyyy-MM-dd HH:mm:ss"
     *
     * @return 时间字符串
     */
    public static String getCurrentDateOfString() {
        return getCurrentDateOfString(STANDARD_PATTERN_19);
    }

    /**
     * 当前时间转换为时间字符串
     *
     * @param formatStr 时间格式
     * @return 时间字符串
     */
    public static String getCurrentDateOfString(String formatStr) {
        return dateToStr(LocalDateTime.now(), formatStr);
    }

    /**
     * 获取当前时间，时间类型为 LocalDateTime
     *
     * @return LocalDateTime
     */
    public static LocalDateTime getCurrentDate() {
        return LocalDateTime.now();
    }


    /**
     * 根据传入的 时间 和 时间格式 转换为时间字符串
     *
     * @param localDateTime 时间
     * @param formatStr     时间字符串
     * @return Date对象
     */
    public static String dateToStr(LocalDateTime localDateTime, String formatStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatStr);
        return formatter.format(localDateTime);
    }

    /**
     * 根据传入的 时间字符串 和 时间格式 转换为时间
     *
     * @param dateTimeStr 时间字符串
     * @param formatStr   时间格式
     * @return LocalDateTime
     */
    public static LocalDateTime strToDate(String dateTimeStr, String formatStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatStr);
        return LocalDateTime.parse(dateTimeStr, formatter);
    }

    /**
     * 根据传入的 时间字符串 和 时间格式 转换为精确到秒的 UTC 时间戳
     *
     * @param dateTimeStr 时间字符串
     * @param pattern     时间格式
     * @return 精确到秒的 UTC 时间戳
     */
    public static Long dateToUtcOfSecond(String dateTimeStr, String pattern) {
        // 定义日期时间格式化器
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        // 解析时间字符串
        LocalDateTime localDateTime = LocalDateTime.parse(dateTimeStr, formatter);
        // 将 LocalDateTime 转换为 ZonedDateTime（指定时区）
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
        // 将 ZonedDateTime 转换为 Instant
        Instant instant = zonedDateTime.toInstant();
        // 转换为以毫秒为单位的UTC时间戳
        return instant.getEpochSecond();
    }

    /**
     * 根据传入的 精确到秒的 UTC 时间戳 和 时间格式 转换为时间字符串
     *
     * @param timestamp UTC时间戳,精确到秒
     * @param pattern   字符串的时间格式
     */
    public static String utcOfSecondToDateStr(Integer timestamp, String pattern) {
        // 将秒级时间戳转换为 Instant
        Instant instant = Instant.ofEpochSecond(timestamp);
        // 将 Instant 转换为 ZonedDateTime（指定时区）
        ZonedDateTime zonedDateTime = instant.atZone(ZoneId.systemDefault());
        // 从 ZonedDateTime 提取 LocalDateTime
        LocalDateTime localDateTime = zonedDateTime.toLocalDateTime();
        // 定义日期时间格式化器
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        // 格式化为字符串
        return localDateTime.format(formatter);
    }

    /**
     * 根据传入的 时间字符串 和 时间格式 转换为精确到毫秒的 UTC 时间戳
     *
     * @param dateTimeStr 时间字符串
     * @param pattern     时间格式
     * @return 精确到毫秒的 UTC 时间戳
     */
    public static Long dateToUTCOfMilli(String dateTimeStr, String pattern) {
        // 定义日期时间格式化器
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        // 解析时间字符串
        LocalDateTime localDateTime = LocalDateTime.parse(dateTimeStr, formatter);
        // 将 LocalDateTime 转换为 ZonedDateTime（指定时区）
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
        // 将 ZonedDateTime 转换为 Instant
        Instant instant = zonedDateTime.toInstant();
        // 转换为以毫秒为单位的UTC时间戳
        return instant.toEpochMilli();
    }

    /**
     * 根据传入的 精确到毫秒的 UTC 时间戳 和 时间格式 转换为时间字符串
     *
     * @param timestamp UTC时间戳,精确到毫秒
     * @param pattern   字符串的时间格式
     */
    public static String utcOfMilliToLocalDateTimeStr(Long timestamp, String pattern) {
        // 将秒级时间戳转换为 Instant
        Instant instant = Instant.ofEpochMilli(timestamp);
        // 将 Instant 转换为 ZonedDateTime（指定时区）
        ZonedDateTime zonedDateTime = instant.atZone(ZoneId.systemDefault());
        // 从 ZonedDateTime 提取 LocalDateTime
        LocalDateTime localDateTime = zonedDateTime.toLocalDateTime();
        // 定义日期时间格式化器
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        // 格式化为字符串
        return localDateTime.format(formatter);
    }

    /**
     * 获取 days 天之后的时间
     *
     * @param days 天数
     * @return LocalDateTime
     */
    public static LocalDateTime getAfterDaysDate(long days) {
        return LocalDateTime.now().plusDays(days);
    }

    /**
     * 获取 hours 小时之后的时间
     *
     * @param hours 小时数
     * @return LocalDateTime
     */
    public static LocalDateTime getAfterHoursDate(long hours) {
        return LocalDateTime.now().plusHours(hours);
    }

    /**
     * 获取 minutes 分钟之后的时间
     *
     * @param minutes 分钟数
     * @return LocalDateTime
     */
    public static LocalDateTime getAfterMinutesDate(long minutes) {
        return LocalDateTime.now().plusMinutes(minutes);
    }

    /**
     * 获取 millis 毫秒之后的时间
     *
     * @param millis 小时数
     * @return LocalDateTime
     */
    public static LocalDateTime getAfterMillisDate(long millis) {
        return LocalDateTime.now().plus(millis, ChronoUnit.MILLIS);
    }

    /**
     * 判断 date日期是否过期(与当前时刻比较)
     *
     * @param date 需要判断的时间
     * @return boolean值
     */
    public static boolean isTimeExpired(LocalDateTime date) {
        return date.isBefore(LocalDateTime.now());
    }

    /**
     * LocalDateTime 转换为 Date
     */
    public static Date localDateTimeToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Date 转换为 LocalDateTime
     */
    public static LocalDateTime dateToLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }
}
