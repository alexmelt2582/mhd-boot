package com.mhd.alert.extern.impl;

import com.mhd.alert.entity.AlertEvent;
import com.mhd.alert.enums.AlertStatusEnum;
import com.mhd.alert.enums.ExtrenAlertEnum;
import com.mhd.alert.extern.ExternAlertService;
import com.mhd.alert.extern.dto.TencentCloudExternAlert;
import com.mhd.alert.reduce.AlarmCommonReduce;
import com.mhd.boot.common.utils.json.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Default external alarm service impl
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TencentExternAlertService implements ExternAlertService {

    private final AlarmCommonReduce alarmCommonReduce;

    @Override
    public void addExternAlert(String content) {
        TencentCloudExternAlert report = JsonUtils.parseObject(content, TencentCloudExternAlert.class);
        if (report == null) {
            log.warn("parse extern alert content failed! content: {}", content);
            return;
        }
        AlertEvent alert = new TencentCloudAlertConverter().convert(report);
        alarmCommonReduce.reduceAndSendAlarm(alert);
    }

    @Override
    public String supportSource() {
        return ExtrenAlertEnum.TENCENT.getCode();
    }

    /**
     * Converter: TencentCloud alert to SingleAlert
     */
    public static class TencentCloudAlertConverter {

        /**
         * Metric alert content template
         */
        private final String metricTemplate = "Tencent Cloud Metric Alert: {metricShowName} {calcType} {calcValue}{calcUnit}, Current: {currentValue}{unit}";

        /**
         * Event alert content template
         */
        private final String eventTemplate = "Tencent Cloud Event Alert: {productShowName} - {eventShowName}";


        /**
         * Convert TencentCloud alert to SingleAlert
         */
        public AlertEvent convert(TencentCloudExternAlert tencentAlert) {
            // Building basic information
            AlertEvent alertEvent = AlertEvent.builder()
                    .status(convertStatus(tencentAlert.getAlarmStatus()))
                    .startAt(parseTime(tencentAlert.getFirstOccurTime()))
                    .activeAt(parseTime(tencentAlert.getFirstOccurTime()))
                    .triggerTimes(1)
                    .build();
            // If it is a recovery state, set the end time.
            if ("0".equals(tencentAlert.getAlarmStatus())) {
                alertEvent.setEndAt(parseTime(tencentAlert.getRecoverTime()));
            }

            // Build labels
            Map<String, String> labels = new HashMap<>();
            buildLabels(labels, tencentAlert);

            // Build annotations
            Map<String, String> annotations = new HashMap<>();
            buildAnnotations(annotations, tencentAlert);

            // Build content
            String content = generateContent(tencentAlert);
            alertEvent.setLabels(labels);
            alertEvent.setAnnotations(annotations);
            alertEvent.setContent(content);
            return alertEvent;
        }

        private void buildLabels(Map<String, String> labels, TencentCloudExternAlert alert) {
            labels.put("__source__", "tencent_cloud");
            labels.put("alert_type", alert.getAlarmType());
            if (TencentCloudExternAlert.METRIC.equals(alert.getAlarmType())) {
                labels.put("metric_name", alert.getAlarmPolicyInfo().getConditions().getMetricName());
                labels.put("namespace", alert.getAlarmObjInfo().getNamespace());
            } else {
                labels.put("event_name", alert.getAlarmPolicyInfo().getConditions().getEventName());
                labels.put("product_name", alert.getAlarmPolicyInfo().getConditions().getProductName());
            }
        }

        private void buildAnnotations(Map<String, String> annotations, TencentCloudExternAlert alert) {
            TencentCloudExternAlert.AlarmPolicyInfo alarmPolicyInfo = alert.getAlarmPolicyInfo();
            TencentCloudExternAlert.AlarmObjInfo alarmObjInfo = alert.getAlarmObjInfo();
            TencentCloudExternAlert.Dimensions dimensions = alert.getAlarmObjInfo().getDimensions();

            putIfNotNull(annotations, "policy_id", alarmPolicyInfo.getPolicyId());
            putIfNotNull(annotations, "policy_type", alarmPolicyInfo.getPolicyType());
            putIfNotNull(annotations, "policy_name", alarmPolicyInfo.getPolicyName());
            putIfNotNull(annotations, "policy_type_cname", alarmPolicyInfo.getPolicyTypeCname());

            putIfNotNull(annotations, "namespace", alarmObjInfo.getNamespace());
            putIfNotNull(annotations, "region", alarmObjInfo.getRegion());
            putIfNotNull(annotations, "app_id", alarmObjInfo.getAppId());
            putIfNotNull(annotations, "uin", alarmObjInfo.getUin());

            putIfNotNull(annotations, "instance_id", dimensions.getUnInstanceId());
            putIfNotNull(annotations, "obj_id", dimensions.getObjId());

        }

        /**
         * Generate alert content with template
         */
        private String generateContent(TencentCloudExternAlert alert) {
            if (TencentCloudExternAlert.METRIC.equals(alert.getAlarmType())) {
                return generateMetricContent(alert);
            } else {
                return generateEventContent(alert);
            }
        }

        /**
         * Generate metric alert content
         */
        private String generateMetricContent(TencentCloudExternAlert alert) {
            TencentCloudExternAlert.Conditions conditions = alert.getAlarmPolicyInfo().getConditions();
            Map<String, String> params = new HashMap<>();

            params.put("metricShowName", conditions.getMetricShowName());

            if (StringUtils.isNotEmpty(conditions.getCalcType())
                    && StringUtils.isNotEmpty(conditions.getCalcValue())) {
                params.put("calcType", conditions.getCalcType());
                params.put("calcValue", conditions.getCalcValue());
                params.put("calcUnit", Objects.toString(conditions.getCalcUnit(), ""));
            } else {
                params.put("calcType", "");
                params.put("calcValue", "N/A");
                params.put("calcUnit", "");
            }

            if (StringUtils.isNotEmpty(conditions.getCurrentValue())) {
                params.put("currentValue", conditions.getCurrentValue());
                params.put("unit", Objects.toString(conditions.getUnit(), ""));
            } else {
                params.put("currentValue", "N/A");
                params.put("unit", "");
            }
            return replacePlaceholders(metricTemplate, params);
        }

        /**
         * Generate event alert content
         */
        private String generateEventContent(TencentCloudExternAlert alert) {
            TencentCloudExternAlert.Conditions conditions = alert.getAlarmPolicyInfo().getConditions();
            Map<String, String> params = new HashMap<>();
            params.put("productShowName", conditions.getProductShowName());
            params.put("eventShowName", conditions.getEventShowName());
            return replacePlaceholders(eventTemplate, params);
        }

        /**
         * Replace placeholders in template with actual values
         */
        private String replacePlaceholders(String template, Map<String, String> params) {
            String result = template;
            for (Map.Entry<String, String> entry : params.entrySet()) {
                result = result.replace("{" + entry.getKey() + "}", Objects.toString(entry.getValue(), "NULL"));
            }
            return result;
        }

        private String convertStatus(String alarmStatus) {
            return "1".equals(alarmStatus) ? AlertStatusEnum.FIRING.getCode() : AlertStatusEnum.RESOLVED.getCode();
        }

        private Long parseTime(String timeStr) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                return sdf.parse(timeStr).getTime();
            } catch (ParseException e) {
                log.error("Failed to parse time: {}", timeStr);
                throw new IllegalArgumentException("Failed to parse time: " + timeStr, e);
            }
        }

        private void putIfNotNull(Map<String, String> map, String key, String value){
            if (StringUtils.isNotEmpty(value)){
                map.put(key, value);
            }
        }
    }
}
