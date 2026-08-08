package com.mhd.alert.extern.impl;

import com.mhd.alert.entity.AlertEvent;
import com.mhd.alert.enums.AlertStatusEnum;
import com.mhd.alert.enums.ExtrenAlertEnum;
import com.mhd.alert.extern.ExternAlertService;
import com.mhd.alert.extern.dto.AlibabaCloudSlsExternAlert;
import com.mhd.alert.reduce.AlarmCommonReduce;
import com.mhd.boot.common.utils.IpDomainUtil;
import com.mhd.boot.common.utils.json.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;

import java.text.MessageFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlibabaCloudSlsExternAlertService implements ExternAlertService {

    private final AlarmCommonReduce alarmCommonReduce;
    private static final AlibabaCloudSlsConverter CONVERTER = new AlibabaCloudSlsConverter();


    @Override
    public void addExternAlert(String content) {
        List<AlibabaCloudSlsExternAlert> externAlerts = new ArrayList<>();
        if (BooleanUtils.isTrue(JsonUtils.isJsonArray(content))) {
            TypeReference<List<AlibabaCloudSlsExternAlert>> typeReference = new TypeReference<>() {
            };
            externAlerts = JsonUtils.parseObject(content, typeReference);
        } else {
            AlibabaCloudSlsExternAlert externAlert = JsonUtils.parseObject(content, AlibabaCloudSlsExternAlert.class);
            if (null != externAlert) {
                externAlerts.add(externAlert);
            }
        }
        if (null == externAlerts || externAlerts.isEmpty()) {
            log.warn("Failure to parse external alert content. content: {}", content);
            return;
        }
        for (AlibabaCloudSlsExternAlert externAlert : externAlerts) {
            AlertEvent alertEvent = CONVERTER.convert(externAlert);
            alarmCommonReduce.reduceAndSendAlarm(alertEvent);
        }
    }

    @Override
    public String supportSource() {
        return ExtrenAlertEnum.ALIBABACLOUD_SLS.getCode();
    }

    /**
     *
     */
    public static class AlibabaCloudSlsConverter {

        public AlertEvent convert(AlibabaCloudSlsExternAlert externAlert) {
            return AlertEvent.builder()
                    .triggerTimes(1)
                    .status(externAlert.getStatus())
                    .startAt(Instant.ofEpochSecond(externAlert.getFireTime()).toEpochMilli())
                    .activeAt(Instant.ofEpochSecond(externAlert.getAlertTime()).toEpochMilli())
                    .endAt(convertResolveTime(externAlert.getStatus(), externAlert.getResolveTime()))
                    .labels(buildLabels(externAlert))
                    .annotations(buildAnnotations(externAlert))
                    .content(formatContent(externAlert))
                    .build();
        }

        /**
         * todo i18n
         *
         * @param externAlert alert content entity
         * @return content
         */
        private String formatContent(AlibabaCloudSlsExternAlert externAlert) {
            // convet severity
            Optional<AlibabaCloudSlsExternAlert.Severity> severity = AlibabaCloudSlsExternAlert.Severity.convert(externAlert.getSeverity());
            // If the alarm state is resolved, the value is the specific recovery time.
            Long resolveTimeMilli = convertResolveTime(externAlert.getStatus(), externAlert.getResolveTime());

            return MessageFormat.format(
                    "AlibabaCloud-sls alert , {0} - [{1}], level: [{2}], desc: {3}, fire_time:{4}, resolve_time:{5}",
                    externAlert.getAnnotation("title"),
                    externAlert.getStatus(),
                    severity.isPresent() ? severity.get().getAlias() : "N/A",
                    externAlert.getAnnotation("desc"),
                    timeSecondToDate(Instant.ofEpochSecond(externAlert.getFireTime()).toEpochMilli()),
                    null != resolveTimeMilli ? timeSecondToDate(resolveTimeMilli) : "N/A"
            );
        }

        /**
         * Converts a timestamp (milliseconds) to a formatted date-time string.
         *
         * @param timestampMillis timestamp in milliseconds
         * @return formatted date-time string in the pattern: yyyy-MM-dd HH:mm:ss
         */
        private String timeSecondToDate(long timestampMillis) {
            LocalDateTime dateTime = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(timestampMillis),
                    ZoneId.systemDefault()
            );
            return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }

        /**
         * Build basic annotations and fill annotations for alibaba cloud sls.
         *
         * @param externAlert alert content entity
         * @return annotations
         */
        private Map<String, String> buildAnnotations(AlibabaCloudSlsExternAlert externAlert) {
            Map<String, String> annotations = new HashMap<>(8);
            Optional<AlibabaCloudSlsExternAlert.Severity> severity = AlibabaCloudSlsExternAlert.Severity.convert(externAlert.getSeverity());
            severity.ifPresent(value -> annotations.put("severity", value.getAlias()));
            // Notification templates for sls need to be configured.
            if (StringUtils.isNotBlank(externAlert.getSigninUrl()) && IpDomainUtil.isHasSchema(externAlert.getSigninUrl())) {
                annotations.put("signinUrl", "<a target=\"_blank\" href=\"" + externAlert.getSigninUrl() + "\">View Details</a>");
            }
            // Filling the annotations with the alibaba cloud sls.
            if (null != externAlert.getAnnotations() && !externAlert.getAnnotations().isEmpty()) {
                annotations.putAll(externAlert.getAnnotations());
            }

            return annotations;
        }

        /**
         * Build basic labels and fill labels for alibaba cloud sls.
         *
         * @param externAlert alert content entity
         * @return labels
         */
        private Map<String, String> buildLabels(AlibabaCloudSlsExternAlert externAlert) {
            Map<String, String> labels = new HashMap<>(8);
            labels.put("__source__", "alibabacloud-sls");
            labels.put("alertname", externAlert.getAlertName());
            labels.put("region", externAlert.getRegion());
            // The project name is globally unique.
            labels.put("project", externAlert.getProject());
            // Filling the labels with the alibaba cloud sls.
            if (null != externAlert.getLabels() && !externAlert.getLabels().isEmpty()) {
                labels.putAll(externAlert.getLabels());
            }
            return labels;
        }

        /**
         * If the alarm status is firing, the value is 0.
         * If the alarm state is resolved, the value is the specific recovery time.
         *
         * @param status            alert status
         * @param resolveTimeSecond recovery time
         * @return milliseconds
         */
        private Long convertResolveTime(String status, int resolveTimeSecond) {
            return AlertStatusEnum.RESOLVED.getCode().equals(status) ? Instant.ofEpochSecond(resolveTimeSecond).toEpochMilli() : null;
        }
    }

}
