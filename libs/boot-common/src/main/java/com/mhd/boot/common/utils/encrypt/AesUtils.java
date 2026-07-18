package com.mhd.boot.common.utils.encrypt;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * AES工具类
 *
 * @author zhao-hao-dong
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AesUtils {
    /**
     * 默认加密密钥。AES 加密密钥默认长度为 16 位。
     * 如果 AES 加密密钥的长度大于或小于 16 位，则会显示一条错误消息
     */
    public static final String DEFAULT_ENCODE_RULES = "tomSun28HaHaHaHa";
    /**
     * 默认算法
     */
    private static final String ALGORITHM_STR = "AES/CBC/PKCS5Padding";
    private static final String AES = "AES";
    /**
     * 加密密钥 AES 加密密钥为 16 位。
     * 如果 AES 加密密钥长度超过 16 位，则会显示一条错误信息
     */
    private static String secretKey = DEFAULT_ENCODE_RULES;

    public static void setDefaultSecretKey(String secretKeyNow) {
        secretKey = secretKeyNow;
    }

    public static String getDefaultSecretKey() {
        return secretKey;
    }

    public static String aesEncode(String content) {
        return aesEncode(content, secretKey);
    }

    public static String aesDecode(String content) {
        return aesDecode(content, secretKey);
    }

    public static boolean isCiphertext(String text) {
        return isCiphertext(text, secretKey);
    }

    /**
     * Encrypted plaintext aes cbc mode
     *
     * @param content    content
     * @param encryptKey secretKey
     * @return ciphertext
     */
    public static String aesEncode(String content, String encryptKey) {
        try {
            // todo consider not init cipher every time and test performance
            SecretKeySpec keySpec = new SecretKeySpec(encryptKey.getBytes(StandardCharsets.UTF_8), AES);
            // cipher based on the algorithm AES
            Cipher cipher = Cipher.getInstance(ALGORITHM_STR);
            // init cipher Encrypt_mode or Decrypt_mode operation, the second parameter is the KEY used
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(encryptKey.getBytes(StandardCharsets.UTF_8)));
            // get content bytes, must utf-8
            byte[] byteEncode = content.getBytes(StandardCharsets.UTF_8);
            // encode content to byte array
            byte[] byteAes = cipher.doFinal(byteEncode);
            // base64 encode content
            return Base64.getEncoder().encodeToString(byteAes);
        } catch (Exception e) {
            log.error("aes encode content error: {}", e.getMessage(), e);
            return content;
        }
    }

    /**
     * Decrypt ciphertext
     *
     * @param content    ciphertext
     * @param decryptKey secretKey
     * @return content
     */
    public static String aesDecode(String content, String decryptKey) {
        try {
            byte[] byteDecode = getBytes(content, decryptKey);
            return new String(byteDecode, StandardCharsets.UTF_8);
        } catch (BadPaddingException e) {
            if (!DEFAULT_ENCODE_RULES.equals(decryptKey)) {
                log.warn("There has default encode secret encode content, try to decode with default secret key");
                return aesDecode(content, DEFAULT_ENCODE_RULES);
            }
            log.error("aes decode content error: {}, please config right common secret key", e.getMessage());
            return content;
        } catch (NoSuchAlgorithmException e) {
            log.error("no such algorithm: {}", e.getMessage(), e);
        } catch (IllegalBlockSizeException e) {
            log.error("illegal block size: {}", e.getMessage(), e);
        } catch (NullPointerException e) {
            log.error("null point exception: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("aes decode error: {}", e.getMessage(), e);
        }
        return content;
    }

    private static byte[] getBytes(final String content, final String decryptKey) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(decryptKey.getBytes(StandardCharsets.UTF_8), AES);
        // cipher based on the algorithm AES
        Cipher cipher = Cipher.getInstance(ALGORITHM_STR);
        // init cipher Encrypt_mode or Decrypt_mode operation, the second parameter is the KEY used
        cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(decryptKey.getBytes(StandardCharsets.UTF_8)));
        // base64 decode content
        byte[] bytesContent = Base64.getDecoder().decode(content);
        // decode content to byte array
        return cipher.doFinal(bytesContent);
    }

    /**
     * Determine whether it is encrypted
     *
     * @param text text
     * @return true false
     */
    public static boolean isCiphertext(String text, String decryptKey) {
        // First use whether it is base64 to determine whether it has been encrypted
        if (Base64Utils.isBase64(text)) {
            // if it is base64, decrypt directly to determine
            try {
                byte[] byteDecode = getBytes(text, decryptKey);
                return byteDecode != null;
            } catch (Exception e) {
                log.warn("isCiphertext method error: {}", e.getMessage());
                return false;
            }
        }
        return false;
    }
}
