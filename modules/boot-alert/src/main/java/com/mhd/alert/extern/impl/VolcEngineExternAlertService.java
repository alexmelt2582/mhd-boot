package com.mhd.alert.extern.impl;

import com.mhd.alert.entity.AlertEvent;
import com.mhd.alert.enums.AlertSeverityEnum;
import com.mhd.alert.enums.AlertStatusEnum;
import com.mhd.alert.enums.ExtrenAlertEnum;
import com.mhd.alert.extern.ExternAlertService;
import com.mhd.alert.extern.dto.VolcEngineExternEventAlert;
import com.mhd.alert.extern.dto.VolcEngineExternMetricAlert;
import com.mhd.alert.reduce.AlarmCommonReduce;
import com.mhd.boot.common.utils.collection.CollectionUtils;
import com.mhd.boot.common.utils.json.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Volcengine alarm entity class
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VolcEngineExternAlertService implements ExternAlertService {

    private final AlarmCommonReduce alarmCommonReduce;
    private static final Map<String, Integer> severityOrder = Map.of(
            AlertSeverityEnum.CRITICAL.getCode(), 1,
            AlertSeverityEnum.WARNING.getCode(), 2,
            AlertSeverityEnum.INFO.getCode(), 3
    );

    @Override
    public void addExternAlert(String content) {
        JsonNode root = JsonUtils.parseTree(content);
        if (root == null) {
            log.warn("parse extern alert content failed! content: {}", content);
            return;
        }
        String type = root.get("Type").asText();
        if (VolcEngineExternMetricAlert.ALERT_TYPE_EVENT.equals(type)) {
            VolcEngineExternEventAlert eventAlert = JsonUtils.parseObject(content, VolcEngineExternEventAlert.class);
            if (eventAlert == null) {
                log.warn("parse extern event alert content failed! content: {}", content);
                return;
            }
            AlertEvent singleAlert = new VolcEngineAlertConverter().convertEventToSingleAlert(eventAlert);
            alarmCommonReduce.reduceAndSendAlarm(singleAlert);

        } else {
            // deal with metric alert
            VolcEngineExternMetricAlert report = JsonUtils.parseObject(content, VolcEngineExternMetricAlert.class);
            if (report == null) {
                log.warn("parse extern metrics alert content failed! content: {}", content);
                return;
            }
            for (AlertEvent singleAlert : new VolcEngineAlertConverter().convertMetricAlertToSingeAlert(report)) {
                alarmCommonReduce.reduceAndSendAlarm(singleAlert);
            }
        }
    }

    @Override
    public String supportSource() {
        return ExtrenAlertEnum.VOLCENGINE.getCode();
    }

    /**
     * Converter: VolcEngine alert to SingleAlert
     */
    public static class VolcEngineAlertConverter {

        /**
         * Convert VolcEngine metric alert to SingleAlert List
         */
        public List<AlertEvent> convertMetricAlertToSingeAlert(VolcEngineExternMetricAlert alert) {
            String status = convertStatus(alert);
            if (Objects.equals(alert.getType(), VolcEngineExternMetricAlert.ALERT_TYPE_METRIC)) {
                return convertMetricAlert(alert, status, alert.getResources());
            }
            if (Objects.equals(alert.getType(), VolcEngineExternMetricAlert.ALERT_TYPE_METRICS_NODATA)) {
                return convertMetricAlert(alert, status, alert.getNoDataResources());
            }
            if (Objects.equals(alert.getType(), VolcEngineExternMetricAlert.ALERT_TYPE_NO_DATA_RECOVERED)) {
                return convertMetricAlert(alert, status, alert.getNoDataRecoveredResources());
            }
            if (Objects.equals(alert.getType(), VolcEngineExternMetricAlert.ALERT_TYPE_METRIC_RECOVERED)) {
                return convertMetricAlert(alert, status, alert.getRecoveredResources());
            }
            return List.of();
        }

        public AlertEvent convertEventToSingleAlert(VolcEngineExternEventAlert alert) {
            AlertEvent alertEvent = AlertEvent.builder()
                    .status(AlertStatusEnum.FIRING.getCode())
                    .startAt(alert.getHappenedAt() * 1000)
                    .activeAt(alert.getHappenedAt() * 1000)
                    .labels(buildEventLabels(alert))
                    .content(alert.getDescriptionCn())
                    .annotations(new HashMap<>())
                    .build();
            return alertEvent;
        }

        /**
         * build labels for volcengine event alert
         */
        private Map<String, String> buildEventLabels(VolcEngineExternEventAlert event) {
            Map<String, String> labels = new HashMap<>();
            labels.put("severity", convertEventSeverity(event));
            labels.put("__source__", "volcengine");
            labels.put("resource_name", event.getDetails().getVolcResourceName());
            labels.put("account_id", event.getAccountId());
            labels.put("region", event.getRegion());
            labels.put("event_type", event.getEventType());
            labels.put("source", event.getSource());
            return labels;

        }

        /**
         * convert volcengine event alert status to heartbeat alert status
         * use the most severe level in the rules
         *
         * @param alert volcengine event alert
         * @return status
         */
        private String convertEventSeverity(VolcEngineExternEventAlert alert) {
            return alert.getRules().stream().map(VolcEngineExternEventAlert.EventRule::getLevel)
                    .min((o1, o2) -> severityOrder.get(convertCommonSeverity(o1)).compareTo(severityOrder.get(convertCommonSeverity(o2))))
                    .orElse(AlertSeverityEnum.INFO.getCode());
        }

        private String convertCommonSeverity(String level) {
            return switch (level) {
                case "critical" -> AlertSeverityEnum.CRITICAL.getCode();
                case "warning" -> AlertSeverityEnum.WARNING.getCode();
                default -> AlertSeverityEnum.INFO.getCode();
            };
        }

        /**
         * create SingleAlert for each resources in volcengine alert
         *
         * @param alert     volcengine alert
         * @param status    status
         * @param resources resources
         * @return List of SingleAlert
         */
        private List<AlertEvent> convertMetricAlert(VolcEngineExternMetricAlert alert, String status,
                                                    List<? extends VolcEngineExternMetricAlert.Resource> resources) {
            if (CollectionUtils.isEmpty(resources)) {
                return List.of();
            }
            List<AlertEvent> result = new ArrayList<>();
            for (VolcEngineExternMetricAlert.Resource resource : resources) {
                AlertEvent alertEvent = AlertEvent.builder()
                        .status(status)
                        .startAt(resource.getFirstAlertTime() * 1000)
                        .endAt(resource.getLastAlertTime() * 1000)
                        .labels(buildLabels(alert, resource))
                        .activeAt(convertHappenAt(alert.getHappenedAt()))
                        .content(resource.getName() + alert.getRuleCondition())
                        .annotations(buildAnnotations(resource))
                        .build();
                result.add(alertEvent);
            }
            return result;
        }

        private Long convertHappenAt(String happenAt) {
            String cleanedStr = happenAt.replace("UTC", "").replace("(", "").replace(")", "");
            DateTimeFormatter altFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssXXX");
            OffsetDateTime odt = OffsetDateTime.parse(cleanedStr, altFormatter);
            return odt.toEpochSecond() * 1000;
        }

        private Map<String, String> buildLabels(VolcEngineExternMetricAlert alert, VolcEngineExternMetricAlert.Resource resource) {
            Map<String, String> labels = new HashMap<>();
            labels.put("__source__", "volcengine");
            labels.put("resource_name", resource.getName());
            labels.put("resource_id", resource.getId());
            labels.put("rule_id", alert.getRuleId());
            return labels;
        }

        private Map<String, String> buildAnnotations(VolcEngineExternMetricAlert.Resource resource) {
            Map<String, String> annotations = new HashMap<>();
            if (resource instanceof VolcEngineExternMetricAlert.NoDataResource noDataResource) {
                for (VolcEngineExternMetricAlert.Metric noDataMetric : noDataResource.getNoDataMetrics()) {
                    annotations.put(noDataMetric.getName(), "N/A");
                }
            }
            for (VolcEngineExternMetricAlert.Metric metric : resource.getMetrics()) {
                annotations.put(metric.getName(), metric.getCurrentValue() + metric.getUnit());
            }
            annotations.put("region", resource.getRegion());
            List<VolcEngineExternMetricAlert.Dimension> dimensions = resource.getDimensions();
            if (CollectionUtils.isNotEmpty(dimensions)) {
                for (VolcEngineExternMetricAlert.Dimension dimension : dimensions) {
                    annotations.put(dimension.getNameCn(), dimension.getValue());
                }
            }
            return annotations;
        }

        /**
         * convert volcengine alert status to heartbeat alert status
         *
         * @param alert volcengine alert
         * @return status
         */
        private String convertStatus(VolcEngineExternMetricAlert alert) {
            String type = alert.getType();
            if (Objects.equals(type, VolcEngineExternMetricAlert.ALERT_TYPE_METRIC)
                    || Objects.equals(type, VolcEngineExternMetricAlert.ALERT_TYPE_EVENT)
                    || Objects.equals(type, VolcEngineExternMetricAlert.ALERT_TYPE_METRICS_NODATA)) {
                return AlertStatusEnum.FIRING.getCode();
            }
            return AlertStatusEnum.RESOLVED.getCode();
        }
    }
}
