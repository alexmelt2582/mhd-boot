package com.mhd.boot.common.idempotent.config;

import com.mhd.boot.common.idempotent.core.key.store.KeyStoreType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author zhao-hao-dong

 */
@ConfigurationProperties("mhd.idempotent")
@Data
public class IdempotentProperties {
    /**
     * 存储类型：redis/memory
     */
    private KeyStoreType keyStoreType = KeyStoreType.MEMORY;
}
