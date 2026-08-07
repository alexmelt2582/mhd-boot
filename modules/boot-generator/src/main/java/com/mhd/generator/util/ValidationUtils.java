package com.mhd.generator.util;

import cn.hutool.core.collection.CollUtil;
import jakarta.validation.*;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;

import java.util.Collection;
import java.util.Set;

/**
 * @author zhao-hao-dong
 * @since 2025-03-10
 **/
public class ValidationUtils {
    private ValidationUtils() {
    }

    private static ValidatorFactory getValidateFactory() {
        // ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        HibernateValidatorConfiguration configure = Validation.byProvider(HibernateValidator.class).configure();
        // 方式一：使用 EL 表达式，pom 文件中需要额外引入 jakarta.el-api 和 jakarta.el
        // failFast 为 true 时，校验到第一个错误就会停止校验，为 false 时会校验完所有属性
        //return configure.failFast(false).buildValidatorFactory();

        // 方式二：不使用 EL 表达式，使用 ParameterMessageInterpolator
        // failFast 为 true 时，校验到第一个错误就会停止校验，为 false 时会校验完所有属性
        return configure
                .failFast(false)
                .messageInterpolator(new ParameterMessageInterpolator())
                .buildValidatorFactory();
    }

    /**
     * 校验传入的多个参数是否为null或空.
     *
     * @param params 要校验的参数列表
     * @throws IllegalArgumentException 如果任意参数为null或空，则抛出异常
     */
    public static void checkNotNullOrEmpty(Object... params) {
        for (int i = 0; i < params.length; i++) {
            if (params[i] == null) {
                throw new IllegalArgumentException("Parameter at index " + i + " cannot be null");
            }
            if (params[i] instanceof String && ((String) params[i]).trim().isEmpty()) {
                throw new IllegalArgumentException("Parameter at index " + i + " cannot be empty string");
            }
            if (params[i] instanceof Collection && ((Collection<?>) params[i]).isEmpty()) {
                throw new IllegalArgumentException("Parameter at index " + i + " cannot be empty collection");
            }
            // 可以继续扩展其他类型的检查
        }
    }

    public static void validate(Object object) {
        if (object == null) {
            throw new IllegalArgumentException("object cannot be null");
        }
        try (ValidatorFactory factory = getValidateFactory()) {
            Validator validator = factory.getValidator();
            Set<ConstraintViolation<Object>> constraintViolations = validator.validate(object);
            if (CollUtil.isNotEmpty(constraintViolations)) {
                throw new ConstraintViolationException(constraintViolations);
            }
        }
    }

    public static void validateByGroup(Object object, Class<?>... groups) {
        if (object == null) {
            throw new IllegalArgumentException("object cannot be null");
        }
        try (ValidatorFactory factory = getValidateFactory()) {
            Validator validator = factory.getValidator();
            Set<ConstraintViolation<Object>> constraintViolations = validator.validate(object, groups);
            if (CollUtil.isNotEmpty(constraintViolations)) {
                throw new ConstraintViolationException(constraintViolations);
            }
        }
    }
}
