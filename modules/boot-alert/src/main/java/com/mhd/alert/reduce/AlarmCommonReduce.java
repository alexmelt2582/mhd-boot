package com.mhd.alert.reduce;

import com.mhd.alert.entity.AlertSingle;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author zhao-hao-dong
 **/
@Component
public class AlarmCommonReduce {
    public void reduceAndSendAlarm(AlertSingle alert) {
        //workerExecutor.execute(reduceAlarmTask(alert));
    }

    public void reduceAndSendAlarmGroup(Map<String, String> groupLabels, List<AlertSingle> alerts) {
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
