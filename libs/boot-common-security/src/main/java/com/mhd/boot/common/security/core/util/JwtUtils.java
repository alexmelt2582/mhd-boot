package com.mhd.boot.common.security.core.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * jjwt工具类
 *
 * @author zhao-hao-dong

 **/
@Slf4j
public class JwtUtils {

    private JwtUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 利用jwt生成token信息.
     *
     * @param claims     数据声明（Claim）其实就是一个Map，比如我们想放入用户名，
     *                   可以简单的创建一个Map然后put进去
     * @param expiration token 过期时间
     * @param secret     用于进行签名的秘钥 这个秘钥还会进行一次base64解码，所以最好长一点
     */
    public static String generateToken(Map<String, Object> claims, Date expiration, String secret) {
        //Assert.notEmpty(claims, "Claims must not be null");
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(expiration) //设置过期的时间
                .signWith(generateSigningKey(secret), SignatureAlgorithm.HS512) // 设置key
                .compact();
    }

    /**
     * 验证token是否过期
     *
     * @param token  要解析的token信息
     * @param secret 用于进行签名的秘钥
     * @return true 表示过期，false表示不过期，如果没有设置过期时间，则也不认为过期
     */
    public static boolean isExpired(String token, String secret) {
        try {
            Claims claims = getClaimsFromToken(token, secret);
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 获取token中的参数值
     *
     * @param token  要解析的token信息
     * @param secret 用于进行签名的秘钥
     */
    public static Map<String, Object> extractInfo(String token, String secret) {
        Claims claims = getClaimsFromToken(token, secret);
        Map<String, Object> info = new HashMap<>(16);
        Set<String> keySet = claims.keySet();
        //通过迭代，提取token中的参数信息
        for (String key : keySet) {
            Object value = claims.get(key);
            info.put(key, value);
        }
        return info;
    }

    /**
     * 利用jwt解析token信息.
     *
     * @param token  要解析的token信息
     * @param secret 用于进行签名的秘钥
     */
    private static Claims getClaimsFromToken(String token, String secret) {
        return Jwts.parserBuilder()
                .setSigningKey(generateSigningKey(secret))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 获取签名密钥
     */
    private static SecretKey generateSigningKey(String secret) {
        try {
            byte[] keyBytes = Decoders.BASE64.decode(secret);
            byte[] digest = MessageDigest.getInstance("SHA-512")
                    .digest(keyBytes);
            return Keys.hmacShaKeyFor(digest);
        } catch (DecodingException e) {
            throw new RuntimeException("传递密钥错误", e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 生成token的过期时间，单位秒
     */
    private static Date generateExpirationDate(Long expiration) {
        return new Date(System.currentTimeMillis() + expiration * 1000);
    }

    public static void main(String[] args) throws Exception {
        String secret = "ZmQ0ZGI5NjQ0MDQwY2I4MjMxY2Y3ZmI3MjdhN2ZmMjNhODViOTg1ZGE0NTBjMGM4NDA5NzYxMjdjOWMwYWRmZTBlZjlhNGY3ZTg4Y2U3YTE1ODVkZDU5Y2Y3OGYwZWE1NzUzNWQ2YjFjZDc0NGMxZWU2MmQ3MjY1NzJmNTE0MzI=";
        Map<String, Object> map = new HashMap<>(16);
        map.put("userId", 123);
        map.put("userName", "java");
        LocalDateTime expireTime = LocalDateTime.now().plusSeconds(604800L);
        String token = JwtUtils.generateToken(map, Date.from(expireTime.atZone(ZoneId.systemDefault()).toInstant()), secret);
        System.out.println("生成token:" + token);
        boolean flag = isExpired(token, secret);
        System.out.println("token是否过期:" + flag);
        Map<String, Object> resultMap = extractInfo(token, secret);
        System.out.println("从token获取参数值:" + resultMap);
    }
}
