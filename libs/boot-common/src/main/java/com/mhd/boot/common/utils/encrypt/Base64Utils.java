package com.mhd.boot.common.utils.encrypt;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Base64工具类
 * @author zhao-hao-dong
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Base64Utils {
    public static boolean isBase64(String base64) {
        if (base64 == null || base64.isEmpty()) {
            return false;
        }
        try {
            return Base64.getDecoder().decode(base64) != null;
        } catch (Exception e) {
            return false;
        }
    }

    public static String encode(String str) {
        return Base64.getEncoder().encodeToString(str.getBytes(StandardCharsets.UTF_8));
    }

    public static String decode(String base64) {
        return new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
    }
}
