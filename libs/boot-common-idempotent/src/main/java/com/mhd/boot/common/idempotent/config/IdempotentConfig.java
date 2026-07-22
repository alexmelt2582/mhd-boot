package com.mhd.boot.common.idempotent.config;

import com.mhd.boot.common.idempotent.aspectj.RepeatSubmitAspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * 幂等功能配置
 *
 * @author zhao-hao-dong
 */
@AutoConfiguration
public class IdempotentConfig {
    @Bean
    public RepeatSubmitAspect repeatSubmitAspect() {
        return new RepeatSubmitAspect();
    }
}
