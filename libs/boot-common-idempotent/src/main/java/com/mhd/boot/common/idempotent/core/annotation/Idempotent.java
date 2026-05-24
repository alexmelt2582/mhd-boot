package com.mhd.boot.common.idempotent.core.annotation;

import com.mhd.boot.common.idempotent.core.key.generator.DefaultIdempotentKeyGenerator;
import com.mhd.boot.common.idempotent.core.key.generator.IdempotentKeyGenerator;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * @author zhao-hao-dong

 */
@Inherited
@Target(ElementType.METHOD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Idempotent {
    /**
     * prefix前缀
     */
    String prefix() default "idem";

    /**
     * 幂等操作的唯一标识，使用spring el表达式 用#来引用方法参数
     */
    String uniqueExpression() default "";

    /**
     * key生成器，默认是 {prefix}:{uniqueExpression.value}
     */
    Class<? extends IdempotentKeyGenerator> keyGenerator() default DefaultIdempotentKeyGenerator.class;

    /**
     * 时长
     */
    long duration() default 1;

    /**
     * 时间单位 默认秒
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 提示信息
     */
    String info() default "重复请求，请稍后重试";

    /**
     * 是否在业务完成后立刻清除幂等key，默认 false
     */
    boolean removeKeyWhenFinished() default false;

    /**
     * 是否在业务执行异常时立刻清除幂等key，默认 false
     */
    boolean removeKeyWhenError() default false;

}
