package com.mhd.alert.extern.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * Tencent cloud alarm entity class
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TencentCloudExternAlert {
    @JsonProperty("sessionID")
    private String sessionId;
    private String alarmStatus;
    private String alarmType;
    private AlarmObjInfo alarmObjInfo;
    private AlarmPolicyInfo alarmPolicyInfo;
    private String firstOccurTime;
    private int durationTime;
    private String recoverTime;

    /**
     * Alarm Object information
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AlarmObjInfo {
        private String region;
        private String namespace;
        @JsonProperty("appID")
        private String appId;
        private String uin;
        private Dimensions dimensions;
    }

    /**
     *  Uniform Resource ID information
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Dimensions {
        @JsonProperty("unInstanceID")
        private String unInstanceId;
        @JsonProperty("objID")
        private String objId;
    }

    /**
     * Alarm policy information
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AlarmPolicyInfo {
        @JsonProperty("policyID")
        private String policyId;
        private String policyType;
        private String policyName;
        @JsonProperty("policyTypeCName")
        private String policyTypeCname;
        private Conditions conditions;
    }

    /**
     * Parameters of indicator alarms
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Conditions {
        // alarm metrics parameters
        private String metricName;
        private String metricShowName;
        private String calcType;
        private String calcValue;
        private String calcUnit;
        private String currentValue;
        private String historyValue;
        private String unit;
        private String period;
        private String periodNum;
        private String alarmNotifyType;
        private long alarmNotifyPeriod;

        // alarm event parameters
        private String productName;
        private String productShowName;
        private String eventName;
        private String eventShowName;
    }

    /**
     * Transaction alarm
     */
    public static final String EVENT = "event";

    /**
     * Indicator alarm
     */
    public static final String METRIC = "metric";

}
