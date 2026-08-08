package com.mhd.alert.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhao-hao-dong
 */
@Getter
@AllArgsConstructor
public enum AlertStatusEnum {
    PENDING("pending", "待定"),
    FIRING("firing", "告警中"),
    RESOLVED("resolved", "已恢复");
    private final String code;
    private final String description;
}
