package com.mhd.boot.common.idempotent.core.key.store;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * @author zhao-hao-dong

 */
public class RedisIdempotentKeyStore implements IdempotentKeyStore {
    private final StringRedisTemplate stringRedisTemplate;

    public RedisIdempotentKeyStore(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean saveIfAbsent(String key, long duration, TimeUnit timeUnit) {
        if (!StringUtils.hasText(key)) {
            return false;
        }
        ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
        return opsForValue.setIfAbsent(key, String.valueOf(System.currentTimeMillis()), duration, timeUnit);
    }

    @Override
    public void remove(String key) {
        stringRedisTemplate.delete(key);
    }
}
