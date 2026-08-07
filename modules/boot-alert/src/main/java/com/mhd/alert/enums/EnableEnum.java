package com.mhd.alert.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhao-hao-dong
 */
@Getter
@AllArgsConstructor
public enum EnableEnum {
    ENABLE(1, "启用"),
    DISABLE(0, "禁用");
    private final int code;
    private final String description;
}
