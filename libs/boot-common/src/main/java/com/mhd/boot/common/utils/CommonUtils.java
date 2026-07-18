package com.mhd.boot.common.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.regex.Pattern;

/**
 * @author zhao-hao-dong
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class CommonUtils {
    /**
     * 数字正则表达式
     */
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^[-+]?[0-9]*\\.?[0-9]+$");

    /**
     * 判断传入的字符串是否为数字
     *
     * @param str string
     * @return true or false
     */
    public static boolean isNumeric(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }
        return NUMBER_PATTERN.matcher(str).matches();
    }

}
