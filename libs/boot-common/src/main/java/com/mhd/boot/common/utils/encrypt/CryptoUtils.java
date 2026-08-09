package com.mhd.boot.common.utils.encrypt;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * 通用加密工具类。
 *
 * <p>提供 SHA256 摘要、HMAC-SHA256 签名等常用密码学计算能力，供告警指纹生成、
 * 钉钉机器人 webhook 签名等场景使用。所有方法均为静态方法，无状态。
 *
 * @author zhao-hao-dong
 **/
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CryptoUtils {

    /**
     * HMAC-SHA256 算法名称
     */
    private static final String ALGORITHM_HMAC_SHA256 = "HmacSHA256";

    private static final Charset UTF8 = StandardCharsets.UTF_8;

    /**
     * 计算字符串的 SHA256 摘要，返回小写十六进制字符串。
     *
     * @param data 待计算摘要的数据
     * @return 64 字符的小写十六进制摘要字符串
     */
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

    /**
     * 计算 HMAC-SHA256 签名并返回 Base64 编码字符串。
     *
     * <p>典型用于钉钉机器人 webhook 的加签模式：以 appSecret 为密钥，
     * 对 {@code "timestamp\nappSecret"} 进行签名，将 timestamp 与签名拼接到 webhook URL。
     *
     * @param key  签名密钥
     * @param data 待签名的数据
     * @return Base64 编码的签名字符串
     */
    public static String hmacSha256Base64(String key, String data) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM_HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(UTF8), mac.getAlgorithm());
            mac.init(secretKeySpec);
            byte[] hmacResult = mac.doFinal(data.getBytes(UTF8));
            return Base64.getEncoder().encodeToString(hmacResult);
        } catch (Exception e) {
            log.error("Failed to calculate HMAC-SHA256: {}", e.getMessage());
            throw new RuntimeException("Failed to calculate HMAC-SHA256", e);
        }
    }

    /**
     * 计算 HMAC-SHA256 签名并返回小写十六进制字符串。
     *
     * <p>典型用于阿里云 SMS API 的 ACS3 签名：以 AccessKeySecret 为密钥，
     * 对规范化请求的 SHA256 摘要进行 HMAC 签名。
     *
     * @param key  签名密钥
     * @param data 待签名的数据
     * @return 小写十六进制签名字符串
     */
    public static String hmacSha256Hex(String key, String data) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM_HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(UTF8), mac.getAlgorithm());
            mac.init(secretKeySpec);
            byte[] hmacResult = mac.doFinal(data.getBytes(UTF8));
            return bytesToHex(hmacResult).toLowerCase();
        } catch (Exception e) {
            log.error("Failed to calculate HMAC-SHA256: {}", e.getMessage());
            throw new RuntimeException("Failed to calculate HMAC-SHA256", e);
        }
    }

    /**
     * 将字节数组转换为小写十六进制字符串。
     *
     * @param bytes 待转换的字节数组
     * @return 小写十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte byteValue : bytes) {
            result.append(String.format("%02x", byteValue));
        }
        return result.toString();
    }
}
