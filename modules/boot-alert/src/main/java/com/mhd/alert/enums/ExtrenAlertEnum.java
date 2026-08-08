package com.mhd.alert.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhao-hao-dong
 **/
@Getter
@AllArgsConstructor
public enum ExtrenAlertEnum {
    DEFAULT("default", "默认外部"),
    PROMETHEUS("prometheus", "Prometheus"),
    ALERTMANAGER("alertmanager", "Prometheus AlertManager"),
    SKYWALKING("skywalking", "SkyWalking 服务"),
    TENCENT("tencent", "腾讯云监控服务"),
    UPTIME_KUMA("uptime-kuma", "Uptime Kuma"),
    ZABBIX("zabbix", "zabbix"),
    ALIBABACLOUD_SLS("alibabacloud-sls", "阿里云日志服务 SLS"),
    HUAWEICLOUD_CES("huaweicloud-ces", "华为云监控服务"),
    VOLCENGINE("volcengine", "火山引擎云监控"),
    ;
    private final String code;
    private final String description;
}