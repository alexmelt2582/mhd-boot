package com.mhd.alert.calculate.periodic;

import cn.hutool.core.map.MapUtil;
import com.mhd.alert.cache.AlarmCacheManager;
import com.mhd.alert.constants.AlertConstants;
import com.mhd.alert.entity.AlertEvent;
import com.mhd.alert.entity.AlertRule;
import com.mhd.alert.enums.AlertStatusEnum;
import com.mhd.alert.enums.EnableEnum;
import com.mhd.alert.reduce.AlarmCommonReduce;
import com.mhd.alert.service.DataSourceService;
import com.mhd.alert.utils.AlertTemplateUtils;
import com.mhd.alert.utils.AlertUtils;
import com.mhd.boot.common.utils.StringUtils;
import com.mhd.boot.common.utils.collection.CollectionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author zhao-hao-dong
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MetricsPeriodicAlertCalculator {
    private static final String VALUE = "__value__";
    private static final String TIMESTAMP = "__timestamp__";

    private final DataSourceService dataSourceService;
    private final AlarmCommonReduce alarmCommonReduce;
    private final AlarmCacheManager alarmCacheManager;

    public void calculate(AlertRule rule) {
        // 1. 前置校验：规则被禁用或表达式为空时直接终止，避免无效查询
        if (Objects.equals(rule.getEnable(), EnableEnum.DISABLE.getCode()) ||
                StringUtils.isBlank(rule.getExpr())) {
            log.error("Alert periodic metrics rule {} is disabled or expression is empty", rule.getName());
            return;
        }
        long currentTimeMilli = System.currentTimeMillis();
        try {
            doCalculate(rule, currentTimeMilli);
        } catch (Exception e) {
            log.error("Calculate periodic metrics rule {} failed: {}", rule.getName(), e.getMessage());
        }
    }

    private void doCalculate(AlertRule rule, long currentTimeMilli) {
        try {
            List<Map<String, Object>> results = dataSourceService.calculate(
                    rule.getDatasource(),
                    rule.getExpr()
            );
            if (CollectionUtils.isEmpty(results)) {
                return;
            }

            for (Map<String, Object> result : results) {
                Map<String, String> fingerPrints = new HashMap<>(8);
                fingerPrints.put(AlertConstants.LABEL_RULE_ID, String.valueOf(rule.getId()));
                fingerPrints.put(AlertConstants.LABEL_ALERT_NAME, rule.getName());
                if (MapUtil.isNotEmpty(rule.getLabels())) fingerPrints.putAll(rule.getLabels());
                for (Map.Entry<String, Object> entry : result.entrySet()) {
                    if (entry.getValue() != null && !VALUE.equals(entry.getKey())
                            && !TIMESTAMP.equals(entry.getKey())) {
                        fingerPrints.put(entry.getKey(), entry.getValue().toString());
                    }
                }

                if (result.get(VALUE) == null) {
                    handleRecoveredAlert(rule.getId(), fingerPrints);
                    continue;
                }

                Map<String, Object> fieldValueMap = new HashMap<>(8);
                if (MapUtil.isNotEmpty(rule.getLabels())) fieldValueMap.putAll(rule.getLabels());
                fieldValueMap.put(AlertConstants.LABEL_ALERT_NAME, rule.getName());
                for (Map.Entry<String, Object> entry : result.entrySet()) {
                    if (entry.getValue() != null) {
                        fieldValueMap.put(entry.getKey(), entry.getValue());
                    }
                }
                afterThresholdRuleMatch(currentTimeMilli, fingerPrints, fieldValueMap, rule);
            }
        } catch (Exception ignored) {
        }
    }

    private void afterThresholdRuleMatch(long currentTimeMilli, Map<String, String> fingerPrints,
                                         Map<String, Object> fieldValueMap, AlertRule rule) {
        Long defineId = rule.getId();
        String fingerprint = AlertUtils.calculateFingerprint(fingerPrints);
        AlertEvent existingAlert = alarmCacheManager.getPending(defineId, fingerprint);
        Map<String, String> labels = new HashMap<>(8);
        fieldValueMap.putAll(rule.getLabels());
        labels.putAll(fingerPrints);
        int requiredTimes = rule.getTimes() == null ? 1 : rule.getTimes();
        if (existingAlert == null) {
            AlertEvent newAlert = AlertEvent.builder()
                    .labels(labels)
                    .annotations(rule.getAnnotations())
                    .content(AlertTemplateUtils.render(rule.getTemplate(), fieldValueMap))
                    .status(AlertStatusEnum.PENDING.getCode())
                    .triggerTimes(1)
                    .startAt(currentTimeMilli)
                    .activeAt(currentTimeMilli)
                    .build();

            if (requiredTimes <= 1) {
                newAlert.setStatus(AlertStatusEnum.FIRING.getCode());
                alarmCacheManager.putFiring(defineId, fingerprint, newAlert);
                alarmCommonReduce.reduceAndSendAlarm(newAlert.clone());
            } else {
                alarmCacheManager.putPending(defineId, fingerprint, newAlert);
            }
        } else {
            existingAlert.setTriggerTimes(existingAlert.getTriggerTimes() + 1);
            existingAlert.setActiveAt(currentTimeMilli);

            if (existingAlert.getStatus().equals(AlertStatusEnum.PENDING.getCode()) && existingAlert.getTriggerTimes() >= requiredTimes) {
                alarmCacheManager.removePending(defineId, fingerprint);
                existingAlert.setStatus(AlertStatusEnum.FIRING.getCode());
                alarmCacheManager.putFiring(defineId, fingerprint, existingAlert);
                alarmCommonReduce.reduceAndSendAlarm(existingAlert.clone());
            }
        }
    }

    private void handleRecoveredAlert(Long defineId, Map<String, String> fingerprints) {
        String fingerprint = AlertUtils.calculateFingerprint(fingerprints);
        AlertEvent firingAlert = alarmCacheManager.removeFiring(defineId, fingerprint);
        if (firingAlert != null) {
            firingAlert.setTriggerTimes(1);
            firingAlert.setEndAt(System.currentTimeMillis());
            firingAlert.setStatus(AlertStatusEnum.RESOLVED.getCode());
            alarmCommonReduce.reduceAndSendAlarm(firingAlert.clone());
        }
        alarmCacheManager.removePending(defineId, fingerprint);
    }
}
