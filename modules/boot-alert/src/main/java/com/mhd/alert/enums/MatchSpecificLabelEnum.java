package com.mhd.alert.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhao-hao-dong
 */
@Getter
@AllArgsConstructor
public enum MatchSpecificLabelEnum {
    SKIP_ALL(0, "跳过匹配"),
    MATCH_SPECIFIC(1, "指定标签匹配");
    private final int code;
    private final String description;
}
