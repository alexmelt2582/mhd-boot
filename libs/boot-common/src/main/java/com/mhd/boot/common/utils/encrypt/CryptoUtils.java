package com.mhd.boot.common.utils.encrypt;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * @author zhao-hao-dong
 **/
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CryptoUtils {
    private static final Charset UTF8 = StandardCharsets.UTF_8;

    public static String sha256Hex(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(data.getBytes(UTF8));
            return bytesToHex(digest).toLowerCase();
        } catch (Exception e) {
            log.error("Failed to calculate SHA256: {}", e.getMessage());
            throw new RuntimeException("Failed to calculate SHA256", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte byteValue : bytes) {
            result.append(String.format("%02x", byteValue));
        }
        return result.toString();
    }
}
