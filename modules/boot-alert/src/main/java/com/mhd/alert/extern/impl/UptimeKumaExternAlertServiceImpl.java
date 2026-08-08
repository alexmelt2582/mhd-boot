package com.mhd.alert.extern.impl;

import com.mhd.alert.entity.AlertEvent;
import com.mhd.alert.enums.AlertStatusEnum;
import com.mhd.alert.enums.ExtrenAlertEnum;
import com.mhd.alert.extern.ExternAlertService;
import com.mhd.alert.extern.dto.UptimeKumaExternAlert;
import com.mhd.alert.reduce.AlarmCommonReduce;
import com.mhd.boot.common.utils.json.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * uptime-kuma external alarm service impl
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UptimeKumaExternAlertServiceImpl implements ExternAlertService {

    private final AlarmCommonReduce alarmCommonReduce;

    @Override
    public void addExternAlert(String content) {
        UptimeKumaExternAlert alert = JsonUtils.parseObject(content, UptimeKumaExternAlert.class);
        if (alert == null) {
            log.warn("parse extern alert content failed! content: {}", content);
            return;
        }
        AlertEvent singleAlert = new UptimeKumaAlertConverter().convert(alert);
        alarmCommonReduce.reduceAndSendAlarm(singleAlert);
    }

    /**
     * Converter: UptimeKuma alert to SingleAlert
     */
    public static class UptimeKumaAlertConverter {

        /**
         * Convert UptimeKuma alert to SingleAlert
         */
        public AlertEvent convert(UptimeKumaExternAlert alert) {
            // build basic info
            AlertEvent alertEvent = AlertEvent.builder()
                    .status(convertStatus(alert.getHeartbeat().getStatus()))
                    .startAt(parseTime(alert.getHeartbeat().getTime()))
                    .activeAt(parseTime(alert.getHeartbeat().getTime()))
                    .triggerTimes(1)
                    .build();

            // build labels
            Map<String, String> labels = new HashMap<>();
            labels.put("__source__", "uptime_kuma");
            labels.put("monitor_id", String.valueOf(alert.getMonitor().getId()));
            labels.put("monitor_name", alert.getMonitor().getName());

            // build annotations
            Map<String, String> annotations = new HashMap<>();
            annotations.put("description", alert.getMonitor().getDescription());
            annotations.put("message", alert.getHeartbeat().getMsg());
            annotations.put("important", String.valueOf(alert.getHeartbeat().isImportant()));

            alertEvent.setLabels(labels);
            alertEvent.setAnnotations(annotations);
            alertEvent.setContent(buildContent(alert));
            return alertEvent;
        }

        private String buildContent(UptimeKumaExternAlert alert) {
            return String.format("Monitor [%s] %s: %s",
                    alert.getMonitor().getName(),
                    alert.getMonitor().getDescription(),
                    alert.getHeartbeat().getMsg());
        }

        private String convertStatus(int status) {
            // uptime kuma status: 1-up, 0-down, 2-pending
            return status == 1 ? AlertStatusEnum.RESOLVED.getCode() : AlertStatusEnum.FIRING.getCode();
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
    }

    @Override
    public String supportSource() {
        return ExtrenAlertEnum.ZABBIX.getCode();
    }
}
