package com.mhd.alert.notice;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * @author zhao-hao-dong
 */
@AllArgsConstructor
@Getter
@ToString
public enum AlertNoticeTypeEnum {
    SMS(0, "sms"),
    EMAIL(1, "email"),
    ;
    private final int code;
    private final String description;
}
