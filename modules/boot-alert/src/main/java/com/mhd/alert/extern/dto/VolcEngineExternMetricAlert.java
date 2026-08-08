package com.mhd.alert.extern.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

/**
 * Volcengine alarm entity class
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VolcEngineExternMetricAlert {

    @JsonProperty("Type")
    private String type;

    @JsonProperty("AccountId")
    private String accountId;

    @JsonProperty("RuleName")
    private String ruleName;

    @JsonProperty("RuleId")
    private String ruleId;

    @JsonProperty("Namespace")
    private String namespace;

    @JsonProperty("SubNamespace")
    private String subNamespace;

    @JsonProperty("Level")
    private String level;

    @JsonProperty("HappenedAt")
    private String happenedAt;

    @JsonProperty("RuleCondition")
    private String ruleCondition;

    @JsonProperty("Resources")
    private List<Resource> resources;

    @JsonProperty("RecoveredResources")
    private List<Resource> recoveredResources;

    @JsonProperty("NoDataResources")
    private List<NoDataResource> noDataResources;

    @JsonProperty("NoDataRecoveredResources")
    private List<NoDataRecoveredResource> noDataRecoveredResources;

    /**
     * alert resource entity class
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Resource {
        @JsonProperty("Id")
        private String id;

        @JsonProperty("Name")
        private String name;

        @JsonProperty("Region")
        private String region;

        @JsonProperty("FirstAlertTime")
        private Long firstAlertTime;

        @JsonProperty("LastAlertTime")
        private Long lastAlertTime;

        @JsonProperty("Metrics")
        private List<Metric> metrics;

        @JsonProperty("AlertGroupId")
        private String alertGroupId;

        @JsonProperty("Dimensions")
        private List<Dimension> dimensions;
    }

    /**
     * alert no data resource entity class
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class NoDataResource extends Resource {
        @JsonProperty("NoDataMetrics")
        private List<Metric> noDataMetrics;
    }

    /**
     * alert no data recovered resource entity class
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class NoDataRecoveredResource extends Resource {
        @JsonProperty("DroppedMetrics")
        private List<DroppedMetric> droppedMetrics;
    }

    /**
     * alert metric entity class
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Metric {
        @JsonProperty("Name")
        private String name;

        @JsonProperty("Unit")
        private String unit;

        @JsonProperty("Threshold")
        private Double threshold;

        @JsonProperty("CurrentValue")
        private Object currentValue;

        @JsonProperty("Description")
        private String description;

        @JsonProperty("Warning")
        private Boolean warning;
    }

    /**
     * alert dropped metric entity class
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class DroppedMetric extends Metric {
        @JsonProperty("Reason")
        private String reason;
    }

    /**
     * resource dimension entity class
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Dimension {
        @JsonProperty("Name")
        private String name;

        @JsonProperty("NameCN")
        private String nameCn;

        @JsonProperty("Value")
        private String value;

        @JsonProperty("Description")
        private String description;
    }


    public static final String ALERT_TYPE_METRIC = "Metric";
    public static final String ALERT_TYPE_EVENT = "Event";
    public static final String ALERT_TYPE_METRIC_RECOVERED = "MetricRecovered";
    public static final String ALERT_TYPE_METRICS_NODATA = "MetricsNoData";
    public static final String ALERT_TYPE_NO_DATA_RECOVERED = "NoDataRecovered";

}




