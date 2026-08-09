package com.mhd.alert.reduce;

import com.mhd.alert.config.AlertThreadPoolConfig;
import com.mhd.alert.entity.AlertEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author zhao-hao-dong
 **/
@Component
@Slf4j
@RequiredArgsConstructor
public class AlarmCommonReduce {
    private final AlertThreadPoolConfig alertThreadPoolConfig;
    private final AlarmGroupReduce alarmGroupReduce;

    private static final Set<String> filterLabels = Set.of(
            "timestamp", "starts_at", "actives_at",
            "end_at", "ends_at", "start_at",
            "active_at"
    );

    public void reduceAndSendAlarm(AlertEvent alertEvent) {
        ThreadPoolExecutor alertReduceWorkerExecutor = alertThreadPoolConfig.getAlertReduceWorkerExecutor();
        alertReduceWorkerExecutor.execute(() -> {
            try {
                String fingerprint = generateAlertFingerprint(alertEvent.getLabels());
                alertEvent.setFingerprint(fingerprint);
                alarmGroupReduce.processGroupAlert(alertEvent);
            } catch (Exception e) {
                log.error("Reduce alarm failed: {}", e.getMessage());
            }
        });
    }

    public void reduceAndSendAlarmGroup(Map<String, String> groupLabels, List<AlertEvent> alertEvents) {
        ThreadPoolExecutor alertReduceWorkerExecutor = alertThreadPoolConfig.getAlertReduceWorkerExecutor();
        alertReduceWorkerExecutor.execute(() -> {
            try {
                for (AlertEvent alertEvent : alertEvents) {
                    String fingerprint = generateAlertFingerprint(alertEvent.getLabels());
                    alertEvent.setFingerprint(fingerprint);
                }
                alarmGroupReduce.processGroupAlert(groupLabels, alertEvents);
            } catch (Exception e) {
                log.error("Reduce alarm group failed: {}", e.getMessage());
            }
        });

    }

    private String generateAlertFingerprint(Map<String, String> labels) {
        return labels.entrySet().stream()
                .filter(e -> !filterLabels.contains(e.getKey()))
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + ":" + e.getValue())
                .collect(Collectors.joining(","));
    }
}
