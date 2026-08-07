package com.mhd.boot.common.utils.date;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * 纯时间工具类 (不包含日期信息)
 * 适用于处理如 "12:30:00" 这样的时间片段
 *
 * @author zhao-hao-dong
 **/
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TimeUtils {
    public static final String PATTERN_HM = "HH:mm";
    public static final String PATTERN_HMS = "HH:mm:ss";

    /**
     * 获取当前系统时间 (仅时间部分)
     *
     * @return LocalTime 对象
     */
    public static LocalTime now() {
        return LocalTime.now();
    }

    /**
     * 获取当前时间字符串，默认格式 HH:mm:ss
     *
     * @return 时间字符串
     */
    public static String nowStr() {
        return now().format(DateTimeFormatter.ofPattern(PATTERN_HMS));
    }

    /**
     * 获取当前时间字符串，指定格式
     *
     * @param pattern 格式，如 "HH:mm"
     * @return 时间字符串
     */
    public static String nowStr(String pattern) {
        return now().format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 将 LocalTime 对象转换为字符串
     *
     * @param time   时间对象
     * @param pattern 格式
     * @return 时间字符串
     */
    public static String format(LocalTime time, String pattern) {
        if (time == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return time.format(formatter);
    }

    /**
     * 将时间字符串解析为 LocalTime 对象
     *
     * @param timeStr 时间字符串 (如 "14:30:00")
     * @param pattern 格式
     * @return LocalTime 对象
     */
    public static LocalTime parse(String timeStr, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return LocalTime.parse(timeStr, formatter);
    }

    /**
     * 获取指定小时后的时间
     *
     * @param hours 小时数 (支持负数)
     * @return LocalTime
     */
    public static LocalTime plusHours(long hours) {
        return now().plusHours(hours);
    }

    /**
     * 获取指定分钟后的时间
     *
     * @param minutes 分钟数 (支持负数)
     * @return LocalTime
     */
    public static LocalTime plusMinutes(long minutes) {
        return now().plusMinutes(minutes);
    }

    /**
     * 计算两个时间之间的差值
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param unit      时间单位 (如 ChronoUnit.MINUTES)
     * @return 差值
     */
    public static long between(LocalTime startTime, LocalTime endTime, ChronoUnit unit) {
        return unit.between(startTime, endTime);
    }

    /**
     * 判断指定时间是否已过 (仅比较时分秒，基于当前系统时间)
     * 注意：如果传入的时间是昨天的 23:00，而现在是今天的 01:00，此方法会返回 false，
     * 因为它只比较时间部分。如果需要跨天比较，请使用 LocalDateTime。
     *
     * @param time 待判断的时间
     * @return true if 当前时间晚于指定时间
     */
    public static boolean isExpired(LocalTime time) {
        return now().isAfter(time);
    }

    /**
     * 判断当前时间是否在两个时间之间 (包含边界)
     * 适用于判断当前是否在营业时间内
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return boolean
     */
    public static boolean isBetweenNow(LocalTime startTime, LocalTime endTime) {
        LocalTime now = now();
        // 处理跨天情况，例如 22:00 到 次日 06:00
        if (startTime.isAfter(endTime)) {
            return !now.isBefore(startTime) || !now.isAfter(endTime);
        }
        return !now.isBefore(startTime) && !now.isAfter(endTime);
    }
}
