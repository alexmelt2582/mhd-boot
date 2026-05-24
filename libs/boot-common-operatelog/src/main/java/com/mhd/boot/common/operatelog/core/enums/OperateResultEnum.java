package com.mhd.boot.common.operatelog.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 操作结果枚举类
 *
 * @author zhao-hao-dong
 **/
@Getter
@AllArgsConstructor
public enum OperateResultEnum {
    /**
     * 成功
     */
    SUCCESS(0),
    /**
     * 失败
     */
    ERROR(1);
    /**
     * 结果码
     */
    private final Integer code;
}