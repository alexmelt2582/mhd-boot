package com.mhd.alert.extern.impl;

import com.mhd.alert.entity.AlertEvent;
import com.mhd.alert.enums.AlertStatusEnum;
import com.mhd.alert.enums.ExtrenAlertEnum;
import com.mhd.alert.extern.ExternAlertService;
import com.mhd.alert.extern.dto.PrometheusExternAlert;
import com.mhd.alert.reduce.AlarmCommonReduce;
import com.mhd.boot.common.utils.json.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tools.jackson.core.type.TypeReference;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Prometheus external alarm service impl
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PrometheusExternAlertService implements ExternAlertService {

    private final AlarmCommonReduce alarmCommonReduce;

    @Override
    public void addExternAlert(String content) {

        TypeReference<List<PrometheusExternAlert>> typeReference = new TypeReference<>() {};
        List<PrometheusExternAlert> alerts = JsonUtils.parseObject(content, typeReference);
        if (alerts == null || alerts.isEmpty()) {
            log.warn("parse prometheus extern alert content failed! content: {}", content);
            return;
        }
        for (PrometheusExternAlert alert : alerts) {
            Map<String, String> annotations = alert.getAnnotations();
            if (annotations == null) {
                annotations = new HashMap<>(8);
            }
            if (StringUtils.hasText(alert.getGeneratorURL())) {
                annotations.put("generatorURL", alert.getGeneratorURL());
            }
            String description = annotations.get("description");
            if (description == null) {
                description = annotations.get("summary");
            }
            if (description == null) {
                description = annotations.values().stream().findFirst().orElse("");
            }
            Map<String, String> labels = alert.getLabels();
            if (labels == null) {
                labels = new HashMap<>(8);
            }
            labels.put("__source__", "prometheus");
            String status = AlertStatusEnum.FIRING.getCode();
            if (alert.getEndsAt() != null && alert.getEndsAt().isBefore(Instant.now())) {
                status = AlertStatusEnum.RESOLVED.getCode();
            }
            AlertEvent singleAlert = AlertEvent.builder()
                    .content(description)
                    .status(status)
                    .activeAt(AlertStatusEnum.FIRING.getCode().equals(status) ? Instant.now().toEpochMilli() : null)
                    .startAt(alert.getStartsAt() != null ? alert.getStartsAt().toEpochMilli() : Instant.now().toEpochMilli())
                    .endAt(AlertStatusEnum.RESOLVED.getCode().equals(status) ? alert.getEndsAt().toEpochMilli() : null)
                    .labels(labels)
                    .annotations(alert.getAnnotations())
                    .triggerTimes(1)
                    .build();

            alarmCommonReduce.reduceAndSendAlarm(singleAlert);
        }
    }

    @Override
    public String supportSource() {
        return ExtrenAlertEnum.PROMETHEUS.getCode();
    }
}
