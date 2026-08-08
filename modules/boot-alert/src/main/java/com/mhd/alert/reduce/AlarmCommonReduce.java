package com.mhd.alert.reduce;

import com.mhd.alert.entity.AlertEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author zhao-hao-dong
 **/
@Component
@Slf4j
public class AlarmCommonReduce {
    public void reduceAndSendAlarm(AlertEvent alert) {
        log.info("receive alert event: {}", alert);
        //workerExecutor.execute(reduceAlarmTask(alert));
    }

    public void reduceAndSendAlarmGroup(Map<String, String> groupLabels, List<AlertEvent> alerts) {
        //workerExecutor.execute(() -> {
        //    try {
        //        // Generate alert fingerprint
        //        for (SingleAlert alert : alerts) {
        //            String fingerprint = generateAlertFingerprint(alert.getLabels());
        //            alert.setFingerprint(fingerprint);
        //        }
        //        // Process the group alert
        //        alarmGroupReduce.processGroupAlert(groupLabels, alerts);
        //    } catch (Exception e) {
        //        log.error("Reduce alarm group failed: {}", e.getMessage());
        //    }
        //});
    }
}
