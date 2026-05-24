package com.mhd.boot.common.idempotent.core.aspect;

import cn.hutool.extra.spring.SpringUtil;
import com.mhd.boot.common.idempotent.core.key.store.IdempotentKeyStore;
import com.mhd.boot.common.idempotent.core.annotation.Idempotent;
import com.mhd.boot.common.idempotent.core.key.generator.IdempotentKeyGenerator;
import com.mhd.boot.common.idempotent.core.exception.IdempotentException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.time.LocalDateTime;

/**
 * @author zhao-hao-dong

 */
@Aspect
public class IdempotentAspect {
    private static final Logger log = LoggerFactory.getLogger(IdempotentAspect.class);

    private final IdempotentKeyStore idempotentKeyStore;

    public IdempotentAspect(final IdempotentKeyStore idempotentKeyStore) {
        this.idempotentKeyStore = idempotentKeyStore;
    }

    @Around("@annotation(idempotent)")
    public Object around(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        IdempotentKeyGenerator idempotentKeyGenerator = SpringUtil.getBean(idempotent.keyGenerator());
        String idempotentKey = idempotentKeyGenerator.generate(idempotent, joinPoint);
        boolean saveSuccess = idempotentKeyStore.saveIfAbsent(idempotentKey, idempotent.duration(), idempotent.timeUnit());
        // 如果保存失败，抛出异常
        Assert.isTrue(saveSuccess, () -> {
            throw new IdempotentException(idempotent.info());
        });
        log.info("[idempotent]:has stored key={}, expireTime={} {}, now={}", idempotentKey, idempotent.duration(),
                idempotent.timeUnit(), LocalDateTime.now());
        // 保存成功正常执行方法
        try {
            Object result = joinPoint.proceed();
            if (idempotent.removeKeyWhenFinished()) {
                idempotentKeyStore.remove(idempotentKey);
                log.info("[idempotent]:has removed key={}", idempotentKey);
            }
            return result;
        } catch (Throwable e) {
            if (idempotent.removeKeyWhenError()) {
                idempotentKeyStore.remove(idempotentKey);
                log.info("[idempotent]:has removed key={}", idempotentKey);
            }
            throw e;
        }
    }
}
