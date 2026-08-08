package com.mhd.alert.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhao-hao-dong
 **/
@Getter
@AllArgsConstructor
public enum AlertSeverityEnum {
    EMERGENCY("emergency", ""),
    CRITICAL("critical", ""),
    WARNING("warning", ""),
    INFO("info", ""),
    ;
    private final String code;
    private final String description;
}