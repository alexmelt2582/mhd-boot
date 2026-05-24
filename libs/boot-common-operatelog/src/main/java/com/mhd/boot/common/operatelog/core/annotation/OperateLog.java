package com.mhd.boot.common.operatelog.core.annotation;

import com.mhd.boot.common.operatelog.core.enums.OperateTypeEnum;

import java.lang.annotation.*;

/**
 * 操作日志注解
 *
 * @author zhao-hao-dong
 **/
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperateLog {
    // ========== 模块字段 ==========

    /**
     * 操作模块
     */
    String module() default "";

    /**
     * 操作描述
     */
    String description() default "";

    /**
     * 操作分类
     */
    OperateTypeEnum type() default OperateTypeEnum.OTHER;

    // ========== 开关字段 ==========

    /**
     * 是否记录操作日志。默认开启
     */
    boolean enable() default true;

    /**
     * 是否记录方法参数。默认记录
     */
    boolean logArgs() default true;

    /**
     * 是否记录方法结果的数据。默认不记录结果
     */
    boolean logResultData() default true;

    /**
     * 排除指定的请求参数
     */
    String[] excludeParamNames() default {};
}
