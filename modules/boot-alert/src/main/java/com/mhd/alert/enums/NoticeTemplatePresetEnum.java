package com.mhd.alert.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通知模板预置类型枚举
 *
 * @author zhao-hao-dong
 */
@Getter
@AllArgsConstructor
public enum NoticeTemplatePresetEnum {
    PRESET(1, "预置模板"),
    CUSTOM(0, "自定义模板");
    private final int code;
    private final String description;
}