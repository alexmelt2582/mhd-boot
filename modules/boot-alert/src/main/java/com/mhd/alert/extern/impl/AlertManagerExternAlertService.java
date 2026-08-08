package com.mhd.alert.extern.impl;

import com.mhd.alert.entity.AlertEvent;
import com.mhd.alert.enums.AlertStatusEnum;
import com.mhd.alert.enums.ExtrenAlertEnum;
import com.mhd.alert.extern.ExternAlertService;
import com.mhd.alert.extern.dto.AlertManagerExternAlert;
import com.mhd.alert.extern.dto.PrometheusExternAlert;
import com.mhd.alert.reduce.AlarmCommonReduce;
import com.mhd.boot.common.utils.json.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertManagerExternAlertService implements ExternAlertService {

    private final AlarmCommonReduce alarmCommonReduce;


    @Override
    public void addExternAlert(String content) {
        AlertManagerExternAlert alert = JsonUtils.parseObject(content, AlertManagerExternAlert.class);
        if (alert == null) {
            log.warn("parse alertmanager extern alert content failed! content: {}", content);
            return;
        }
        List<PrometheusExternAlert> alerts = alert.getAlerts();
        if (alerts == null || alerts.isEmpty()) {
            log.warn("receive alertmanager extern alert without alerts! content: {}", content);
            return;
        }
        for (PrometheusExternAlert prometheusAlert : alerts) {
            Map<String, String> annotations = prometheusAlert.getAnnotations();
            if (annotations == null) {
                annotations = new HashMap<>(8);
            }
            if (StringUtils.hasText(prometheusAlert.getGeneratorURL())) {
                annotations.put("generatorURL", prometheusAlert.getGeneratorURL());
            }
            String description = annotations.get("description");
            if (description == null) {
                description = annotations.get("summary");
            }
            if (description == null) {
                description = annotations.values().stream().findFirst().orElse("");
            }
            Map<String, String> labels = prometheusAlert.getLabels();
            if (labels == null) {
                labels = new HashMap<>(8);
            }
            labels.put("__source__", "alertmanager");
            String status = AlertStatusEnum.FIRING.getCode();
            Instant now = Instant.now();
            Instant endsAt = prometheusAlert.getEndsAt();
            if (endsAt != null && endsAt.getEpochSecond() > 0 && endsAt.isBefore(now)) {
                status = AlertStatusEnum.RESOLVED.getCode();
            }
            AlertEvent alertEvent = AlertEvent.builder()
                    .content(description)
                    .status(status)
                    .activeAt(AlertStatusEnum.FIRING.getCode().equals(status) ? Instant.now().toEpochMilli() : null)
                    .startAt(prometheusAlert.getStartsAt() != null ? prometheusAlert.getStartsAt().toEpochMilli() : Instant.now().toEpochMilli())
                    .endAt(AlertStatusEnum.RESOLVED.getCode().equals(status) ? prometheusAlert.getEndsAt().toEpochMilli() : null)
                    .labels(labels)
                    .annotations(prometheusAlert.getAnnotations())
                    .triggerTimes(1)
                    .build();

            alarmCommonReduce.reduceAndSendAlarm(alertEvent);
        }
    }

    @Override
    public String supportSource() {
        return ExtrenAlertEnum.ALERTMANAGER.getCode();
    }
}
