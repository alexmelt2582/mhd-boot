package com.mhd.alert.cache;

import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.mhd.alert.constants.AlertConstants;
import com.mhd.alert.entity.AlertEvent;
import com.mhd.alert.enums.AlertStatusEnum;
import com.mhd.alert.service.AlertEventService;
import com.mhd.alert.utils.AlertUtils;
import com.mhd.boot.common.utils.collection.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhao-hao-dong
 **/
@Component
public class AlarmCacheManager {
    private static final String CUSTOM_FIRING_ROW_KEY = "CUSTOM_FIRING_";

    private final Table<String, String, AlertEvent> pendingAlertMap;

    private final Table<String, String, AlertEvent> firingAlertMap;

    public AlarmCacheManager(AlertEventService alertEventService) {
        this.pendingAlertMap = Tables.newCustomTable(new ConcurrentHashMap<>(8), ConcurrentHashMap::new);
        this.firingAlertMap = Tables.newCustomTable(new ConcurrentHashMap<>(8), ConcurrentHashMap::new);
        List<AlertEvent> firingAlertEvents = alertEventService.selectListByStatus(AlertStatusEnum.FIRING.getCode());
        if (CollectionUtils.isNotEmpty(firingAlertEvents)) {
            for (AlertEvent firingAlertEvent : firingAlertEvents) {
                String fingerprint = AlertUtils.calculateFingerprint(firingAlertEvent.getLabels());
                String ruleId = firingAlertEvent.getLabels().get(AlertConstants.LABEL_RULE_ID);
                if (com.mhd.boot.common.utils.StringUtils.isBlank(ruleId)) {
                    ruleId = getCustomKey(fingerprint);
                }
                firingAlertEvent.setId(null);
                this.firingAlertMap.put(ruleId, fingerprint, firingAlertEvent);
            }
        }
    }

    public void putPending(Long ruleId, String fingerPrint, AlertEvent alertEvent) {
        this.pendingAlertMap.put(String.valueOf(ruleId), fingerPrint, alertEvent);
    }

    public AlertEvent getPending(Long ruleId, String fingerPrint) {
        return this.pendingAlertMap.get(String.valueOf(ruleId), fingerPrint);
    }

    public void removePending(Long ruleId, String fingerPrint) {
        this.pendingAlertMap.remove(String.valueOf(ruleId), fingerPrint);
    }

    public void putFiring(Long ruleId, String fingerPrint, AlertEvent alertEvent) {
        this.firingAlertMap.put(String.valueOf(ruleId), fingerPrint, alertEvent);
    }

    public void putFiring(String fingerPrint, AlertEvent alertEvent) {
        this.firingAlertMap.put(getCustomKey(fingerPrint), fingerPrint, alertEvent);
    }

    public AlertEvent getFiring(Long ruleId, String fingerPrint) {
        AlertEvent alertEvent = this.firingAlertMap.get(String.valueOf(ruleId), fingerPrint);
        if (null != alertEvent) {
            return alertEvent;
        }
        return getFiring(fingerPrint);
    }

    public AlertEvent removeFiring(Long ruleId, String fingerPrint) {
        AlertEvent alertEvent = this.firingAlertMap.remove(String.valueOf(ruleId), fingerPrint);
        if (null == alertEvent) {
            return this.firingAlertMap.remove(getCustomKey(fingerPrint), fingerPrint);
        }
        return alertEvent;
    }

    public AlertEvent getFiring(String fingerPrint) {
        return this.firingAlertMap.get(getCustomKey(fingerPrint), fingerPrint);
    }

    private String getCustomKey(String fingerPrint) {
        return CUSTOM_FIRING_ROW_KEY + fingerPrint;
    }

    public AlertEvent removeFiring(String fingerPrint) {
        return this.firingAlertMap.remove(getCustomKey(fingerPrint), fingerPrint);
    }
}
