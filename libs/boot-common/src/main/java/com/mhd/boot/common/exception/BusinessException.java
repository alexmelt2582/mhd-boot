package com.mhd.boot.common.exception;

import cn.hutool.core.text.StrFormatter;
import com.mhd.boot.common.enums.ErrorCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * 业务异常（支持占位符 {} ）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class BusinessException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 错误码
     */
    private String code;
    /**
     * 错误提示
     */
    private String message;

    /**
     * 错误明细，内部调试错误
     */
    private String detailMessage;

    public BusinessException(String message, Object... args) {
        this(ErrorCodeEnum.FAIL, StrFormatter.format(message, args));
    }

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ErrorCodeEnum errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BusinessException(ErrorCodeEnum errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }
}