package com.mhd.alert.store;

import com.mhd.alert.entity.AlertEvent;
import com.mhd.alert.entity.AlertGroup;
import com.mhd.alert.enums.AlertStatusEnum;
import com.mhd.alert.mapper.AlertEventMapper;
import com.mhd.alert.mapper.AlertGroupMapper;
import com.mhd.alert.service.AlertEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 告警数据持久化-数据库存储
 *
 * @author zhao-hao-dong
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DbAlertStoreHandlerImpl implements AlertStoreHandler {
    /**
     * 分段锁数量，用于按 key 分散并发写入冲突
     */
    private static final int LOCK_STRIPE_COUNT = 256;

    /**
     * 基于条带化策略预创建的锁对象数组
     */
    private static final Object[] KEY_LOCKS = createKeyLocks();

    /**
     * 单条告警持久化访问组件
     */
    private final AlertEventService alertEventService;
    private final AlertEventMapper alertEventMapper;

    /**
     * 告警分组持久化访问组件
     */
    private final AlertGroupMapper alertGroupMapper;

    /**
     * 持久化告警分组及其告警明细，并在存在历史记录时合并必要状态信息
     *
     * @param alertGroup 待持久化的告警分组
     * @return 持久化后的告警分组；当入参为空或无告警明细时返回原对象
     */
    @Override
    public AlertGroup store(AlertGroup alertGroup) {
        // 1. 如果告警组不存在或者告警组中的告警列表为空，则直接返回
        if (alertGroup == null || alertGroup.getAlerts() == null || alertGroup.getAlerts().isEmpty()) {
            log.error("The Group Alerts is empty, ignore store");
            return alertGroup;
        }

        Set<String> alertFingerprints = new HashSet<>(8);
        List<AlertEvent> originalAlerts = alertGroup.getAlerts();
        List<AlertEvent> newAlerts = new ArrayList<>();

        for (AlertEvent alertEvent : originalAlerts) {
            synchronized (lockFor(alertEvent.getFingerprint())) {
                AlertEvent existAlert = alertEventService.selectByFingerprint(alertEvent.getFingerprint());
                if (existAlert != null) {
                    alertEvent.setId(existAlert.getId());
                    alertEvent.setCreateTime(existAlert.getCreateTime());
                    if (AlertStatusEnum.FIRING.getCode().equals(alertEvent.getStatus())) {
                        // 如果当前告警状态为触发中，并且数据库中存在的告警状态不是已恢复，则更新开始时间和触发次数
                        if (!AlertStatusEnum.RESOLVED.getCode().equals(existAlert.getStatus())) {
                            alertEvent.setStartAt(existAlert.getStartAt());
                            int triggerTimes = Optional.ofNullable(existAlert.getTriggerTimes()).orElse(1)
                                    + Optional.ofNullable(alertEvent.getTriggerTimes()).orElse(1);
                            alertEvent.setTriggerTimes(triggerTimes);
                        }
                    } else if (AlertStatusEnum.RESOLVED.getCode().equals(alertEvent.getStatus())) {
                        // 如果当前告警状态为已恢复，并且数据库中存在的告警状态是触发中，则更新结束时间
                        if (AlertStatusEnum.FIRING.getCode().equals(existAlert.getStatus())) {
                            alertEvent.setEndAt(System.currentTimeMillis());
                        }
                        alertEvent.setStartAt(existAlert.getStartAt());
                        alertEvent.setActiveAt(existAlert.getActiveAt());
                        alertEvent.setTriggerTimes(existAlert.getTriggerTimes());
                    }
                }
                alertEventMapper.insertOrUpdate(alertEvent);
                newAlerts.add(alertEvent);
                alertFingerprints.add(alertEvent.getFingerprint());
            }
        }
        alertGroup.setAlerts(newAlerts);
        synchronized (lockFor(alertGroup.getGroupKey())) {
            AlertGroup existAlertGroup = alertGroupMapper.selectByGroupKey(alertGroup.getGroupKey());
            if (existAlertGroup != null) {
                List<String> existFingerprints = existAlertGroup.getAlertFingerprints();
                if (existFingerprints != null && !existFingerprints.isEmpty()) {
                    alertFingerprints.addAll(existFingerprints);
                }
                alertGroup.setId(existAlertGroup.getId());
                alertGroup.setCreateTime(existAlertGroup.getCreateTime());
                Map<String, String> existCommonLabels = existAlertGroup.getCommonLabels();
                if (existCommonLabels != null) {
                    Map<String, String> commonLabels = alertGroup.getCommonLabels();
                    if (commonLabels != null) {
                        commonLabels = commonLabels.entrySet().stream()
                                .filter(entry -> existCommonLabels.containsKey(entry.getKey()))
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                        alertGroup.setCommonLabels(commonLabels);
                    }
                }
                Map<String, String> existCommonAnnotations = existAlertGroup.getCommonAnnotations();
                if (existCommonAnnotations != null) {
                    Map<String, String> commonAnnotations = alertGroup.getCommonAnnotations();
                    if (commonAnnotations != null) {
                        commonAnnotations = commonAnnotations.entrySet().stream()
                                .filter(entry -> existCommonAnnotations.containsKey(entry.getKey()))
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                        alertGroup.setCommonAnnotations(commonAnnotations);
                    }
                }
            }
        }
        alertGroup.setAlertFingerprints(alertFingerprints.stream().toList());
        alertGroupMapper.insertOrUpdate(alertGroup);
        return alertGroup;
    }

    /**
     * 初始化条带锁数组
     *
     * @return 按 LOCK_STRIPE_COUNT 创建的锁对象数组
     */
    private static Object[] createKeyLocks() {
        Object[] locks = new Object[LOCK_STRIPE_COUNT];
        for (int index = 0; index < locks.length; index++) {
            locks[index] = new Object();
        }
        return locks;
    }

    /**
     * 根据业务 key 定位对应的条带锁
     *
     * @param key 业务唯一键
     * @return 与该 key 映射的锁对象
     */
    private static Object lockFor(String key) {
        return KEY_LOCKS[Math.floorMod(key.hashCode(), LOCK_STRIPE_COUNT)];
    }
}
