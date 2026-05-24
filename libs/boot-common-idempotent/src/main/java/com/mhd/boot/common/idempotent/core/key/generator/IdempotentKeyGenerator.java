package com.mhd.boot.common.idempotent.core.key.generator;

import com.mhd.boot.common.idempotent.core.annotation.Idempotent;
import org.aspectj.lang.JoinPoint;
import org.springframework.lang.NonNull;

/**
 * @author zhao-hao-dong

 */
@FunctionalInterface
public interface IdempotentKeyGenerator {
    /**
     * 生成唯一 key
     * @param idempotent 接口注解标识
     * @param point 接口切点信息
     * @return 处理结果
     */
    @NonNull
    String generate(Idempotent idempotent, JoinPoint point);
}
