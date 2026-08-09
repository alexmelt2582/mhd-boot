package com.mhd.alert.reduce;

import com.mhd.alert.entity.AlertEvent;
import com.mhd.alert.entity.AlertGroup;
import com.mhd.alert.enums.AlertStatusEnum;
import com.mhd.boot.common.utils.collection.CollectionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 告警分组收敛器。
 *
 * <p>位于收敛链路最前端，负责将单条或批量 {@link AlertEvent} 组装为
 * {@link AlertGroup}，并委托 {@link AlarmInhibitReduce} 进入抑制→静默→分发流程。
 *
 * <p>分组逻辑采用「按公共标签聚合」的策略：
 * <ul>
 *   <li>单条告警：以其非易失标签作为 groupLabels，单独成组；</li>
 *   <li>批量告警：以调用方传入的 groupLabels 作为分组键，整批成组。</li>
 * </ul>
 *
 * <p>易失标签（时间戳类）不参与分组键计算，避免相同语义告警因时间不同而被拆散。
 *
 * @author zhao-hao-dong
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AlarmGroupReduce {

    /**
     * 不参与分组键计算的易失标签集合（时间戳类标签，值随每次触发变化）
     */
    private static final Set<String> VOLATILE_LABELS = Set.of(
            "timestamp", "starts_at", "actives_at",
            "end_at", "ends_at", "start_at", "active_at"
    );

    private final AlarmInhibitReduce alarmInhibitReduce;

    /**
     * 处理单条告警：将其组装为告警组并进入抑制收敛流程。
     *
     * <p>执行流程：
     * <ol>
     *   <li>空告警直接跳过，避免下游空指针；</li>
     *   <li>以告警非易失标签构建 groupLabels，并据此生成 groupKey；</li>
     *   <li>组装 {@link AlertGroup}，沿用告警自身的状态与注解；</li>
     *   <li>委托 {@link AlarmInhibitReduce#inhibitAlarm(AlertGroup)} 进入抑制流程。</li>
     * </ol>
     *
     * @param alertEvent 待处理的单条告警
     */
    public void processGroupAlert(AlertEvent alertEvent) {
        // 1. 空告警直接跳过，避免下游空指针
        if (alertEvent == null) {
            return;
        }
        // 2. 以告警非易失标签构建 groupLabels，并据此生成 groupKey
        Map<String, String> groupLabels = extractGroupLabels(alertEvent.getLabels());
        String groupKey = generateGroupKey(groupLabels);
        // 3. 组装告警组，沿用告警自身的状态与注解
        AlertGroup alertGroup = AlertGroup.builder()
                .groupKey(groupKey)
                .status(alertEvent.getStatus())
                .groupLabels(groupLabels)
                .commonLabels(alertEvent.getLabels())
                .commonAnnotations(alertEvent.getAnnotations())
                .alertFingerprints(alertEvent.getFingerprint() == null
                        ? Collections.emptyList()
                        : List.of(alertEvent.getFingerprint()))
                .alerts(new ArrayList<>(List.of(alertEvent)))
                .build();
        // 4. 委托抑制流程进入后续收敛
        alarmInhibitReduce.inhibitAlarm(alertGroup);
    }

    /**
     * 处理批量告警：将整批告警按指定分组键组装为告警组并进入抑制收敛流程。
     *
     * <p>执行流程：
     * <ol>
     *   <li>空批次直接跳过，避免无效收敛；</li>
     *   <li>以调用方传入的 groupLabels 生成 groupKey，作为整组收敛键；</li>
     *   <li>聚合整批告警的公共标签与公共注解，以及指纹列表；</li>
     *   <li>按是否有 FIRING 告警决定组状态，组装 {@link AlertGroup}；</li>
     *   <li>委托 {@link AlarmInhibitReduce#inhibitAlarm(AlertGroup)} 进入抑制流程。</li>
     * </ol>
     *
     * @param groupLabels 分组标签，作为整组收敛键
     * @param alertEvents 待处理的批量告警
     */
    public void processGroupAlert(Map<String, String> groupLabels, List<AlertEvent> alertEvents) {
        // 1. 空批次直接跳过，避免无效收敛
        if (CollectionUtils.isEmpty(alertEvents)) {
            return;
        }
        // 2. 以调用方传入的 groupLabels 生成 groupKey，作为整组收敛键
        Map<String, String> safeGroupLabels = groupLabels == null ? new HashMap<>(0) : groupLabels;
        String groupKey = generateGroupKey(safeGroupLabels);
        // 3. 聚合整批告警的公共标签与公共注解，以及指纹列表
        Map<String, String> commonLabels = extractCommonAttributes(alertEvents, AlertEvent::getLabels);
        Map<String, String> commonAnnotations = extractCommonAttributes(alertEvents, AlertEvent::getAnnotations);
        List<String> fingerprints = alertEvents.stream()
                .map(AlertEvent::getFingerprint)
                .filter(f -> f != null && !f.isEmpty())
                .collect(Collectors.toList());
        // 4. 按是否有 FIRING 告警决定组状态：只要有一条触发中即为 firing
        String status = alertEvents.stream()
                .anyMatch(e -> AlertStatusEnum.FIRING.getCode().equals(e.getStatus()))
                ? AlertStatusEnum.FIRING.getCode() : AlertStatusEnum.RESOLVED.getCode();
        AlertGroup alertGroup = AlertGroup.builder()
                .groupKey(groupKey)
                .status(status)
                .groupLabels(safeGroupLabels)
                .commonLabels(commonLabels)
                .commonAnnotations(commonAnnotations)
                .alertFingerprints(fingerprints)
                .alerts(new ArrayList<>(alertEvents))
                .build();
        // 5. 委托抑制流程进入后续收敛
        alarmInhibitReduce.inhibitAlarm(alertGroup);
    }

    /**
     * 从告警标签中剔除易失标签，得到用于分组的稳定标签集合。
     *
     * @param labels 原始告警标签
     * @return 剔除易失标签后的分组标签
     */
    private Map<String, String> extractGroupLabels(Map<String, String> labels) {
        if (labels == null || labels.isEmpty()) {
            return new HashMap<>(0);
        }
        Map<String, String> groupLabels = new LinkedHashMap<>(labels.size());
        for (Map.Entry<String, String> entry : labels.entrySet()) {
            // 仅保留非易失标签，保证分组键稳定
            if (!VOLATILE_LABELS.contains(entry.getKey())) {
                groupLabels.put(entry.getKey(), entry.getValue());
            }
        }
        return groupLabels;
    }

    /**
     * 提取一批告警中所有告警共有的标签（键值均相同），作为整组公共标签。
     *
     * @param alertEvents 告警列表
     * @param extractor   标签或注解提取函数
     * @return 公共标签集合，空列表返回空 map
     */
    private Map<String, String> extractCommonAttributes(
            List<AlertEvent> alertEvents,
            java.util.function.Function<AlertEvent, Map<String, String>> extractor) {
        if (CollectionUtils.isEmpty(alertEvents)) {
            return new HashMap<>(0);
        }
        Map<String, String> first = extractor.apply(alertEvents.get(0));
        Map<String, String> common = first == null ? new HashMap<>(0) : new HashMap<>(first);
        for (int i = 1; i < alertEvents.size(); i++) {
            Map<String, String> current = extractor.apply(alertEvents.get(i));
            // 逐步剔除不一致或缺失的键，保留所有告警共有且相同的键值对
            common.keySet().removeIf(key ->
                    current == null || !current.containsKey(key)
                            || !String.valueOf(common.get(key)).equals(String.valueOf(current.get(key))));
        }
        return common;
    }

    /**
     * 根据分组标签生成稳定的 groupKey：键排序后拼接为 "k1:v1,k2:v2"。
     *
     * @param groupLabels 分组标签
     * @return 分组键字符串
     */
    private String generateGroupKey(Map<String, String> groupLabels) {
        if (groupLabels == null || groupLabels.isEmpty()) {
            return "";
        }
        return groupLabels.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + ":" + e.getValue())
                .collect(Collectors.joining(","));
    }
}
