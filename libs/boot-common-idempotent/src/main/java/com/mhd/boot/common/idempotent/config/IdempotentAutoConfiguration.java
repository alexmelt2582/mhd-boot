package com.mhd.boot.common.idempotent.config;

import cn.hutool.extra.spring.EnableSpringUtil;
import com.mhd.boot.common.idempotent.core.key.generator.DefaultIdempotentKeyGenerator;
import com.mhd.boot.common.idempotent.core.key.generator.IdempotentKeyGenerator;
import com.mhd.boot.common.idempotent.core.key.store.KeyStoreType;
import com.mhd.boot.common.idempotent.core.key.store.MemoryIdempotentKeyStore;
import com.mhd.boot.common.idempotent.core.key.store.RedisIdempotentKeyStore;
import com.mhd.boot.common.idempotent.core.aspect.IdempotentAspect;
import com.mhd.boot.common.idempotent.core.key.store.IdempotentKeyStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author zhao-hao-dong

 */
@Configuration
@EnableConfigurationProperties({IdempotentProperties.class})
@EnableSpringUtil
public class IdempotentAutoConfiguration {

    @Bean
    public IdempotentKeyGenerator idempotentKeyGenerator() {
        return new DefaultIdempotentKeyGenerator();
    }

    @Bean
    @ConditionalOnMissingBean
    public IdempotentKeyStore idempotentKeyStore(IdempotentProperties properties, StringRedisTemplate stringRedisTemplate) {
        KeyStoreType keyStoreType = properties.getKeyStoreType();
        return keyStoreType.equals(KeyStoreType.REDIS) ? new RedisIdempotentKeyStore(stringRedisTemplate) : new MemoryIdempotentKeyStore();
    }

    @Bean
    public IdempotentAspect idempotentAspect(IdempotentKeyStore idempotentKeyStore) {
        return new IdempotentAspect(idempotentKeyStore);
    }
}
