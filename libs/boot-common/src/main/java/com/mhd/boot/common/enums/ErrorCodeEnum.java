package com.mhd.boot.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错误码枚举类
 *
 * @author zhao-hao-dong
 **/
@Getter
@AllArgsConstructor
public enum ErrorCodeEnum {
    /**
     * 错误
     */
    ERROR_500("500", "服务器未知错误"),
    ERROR_400("400", "错误请求"),

    /**
     * OK：操作成功
     */
    SUCCESS("200", "操作成功"),
    FAIL("-1", "操作失败"),

    VALID_FAILED("A0000", "参数校验失败"),
    ;
    /**
     * 状态码
     */
    private final String code;

    /**
     * 信息
     */
    private final String message;
}
