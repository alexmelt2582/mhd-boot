package com.mhd.alert.enums;

import com.mhd.boot.common.utils.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhao-hao-dong
 */
@Getter
@AllArgsConstructor
public enum AlertRuleTypeEnum {
    REALTIME_METRIC("realtime_metric", "实时指标告警"),
    PERIODIC_METRIC("periodic_metric", "周期指标告警"),
    REALTIME_LOG("realtime_log", "实时日志告警"),
    PERIODIC_LOG("periodic_log", "周期日志告警"),
    ;
    private final String code;
    private final String description;

    public static boolean isPeriodicType(String type) {
        if (StringUtils.isBlank(type)) {
            return false;
        }
        return PERIODIC_METRIC.getCode().equals(type) || PERIODIC_LOG.getCode().equals(type);
    }
}
