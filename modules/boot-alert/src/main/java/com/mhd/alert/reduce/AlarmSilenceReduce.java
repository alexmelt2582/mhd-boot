package com.mhd.alert.reduce;

import com.mhd.alert.cache.AlertCacheFactory;
import com.mhd.alert.entity.AlertGroup;
import com.mhd.alert.entity.AlertSilence;
import com.mhd.alert.notice.AlertNoticeDispatch;
import com.mhd.alert.service.AlertSilenceService;
import com.mhd.boot.common.utils.StringUtils;
import com.mhd.boot.common.utils.collection.CollectionUtils;
import com.mhd.boot.common.utils.date.TimeUtils;
import com.mhd.boot.common.utils.json.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 告警静默收敛器。
 *
 * <p>参照 Prometheus 告警静默机制：当告警组命中「启用」的静默规则时，在静默时段内
 * 抑制其通知下发（不下发至通知处理器），但不会丢弃告警本身。典型场景——计划内运维窗口
 * 期间临时屏蔽已知噪音告警。
 *
 * <p>执行链路位置：{@link AlarmInhibitReduce} → <b>此处</b> → {@link AlertNoticeDispatch}。
 *
 * <p>静默规则支持两种类型：
 * <ul>
 *   <li>{@code type=0} 一次性：仅在 {@code periodStart ~ periodEnd} 时段内静默，
 *       命中后累计 {@code times} 计数并落库；</li>
 *   <li>{@code type=1} 周期性：需同时满足「生效星期」与「时段」两个条件才静默。</li>
 * </ul>
 *
 * <p>静默规则通过 {@link AlertCacheFactory#getAlertSilenceCache()} 缓存，未命中时回源
 * 数据库并回填缓存；规则变更后应调用 {@link AlertCacheFactory#clearAlertSilenceCache()} 失效。
 *
 * @author zhao-hao-dong
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AlarmSilenceReduce {

    /**
     * 一次性静默类型
     */
    private static final int TYPE_ONE_TIME = 0;

    /**
     * 周期性静默类型
     */
    private static final int TYPE_CYCLIC = 1;

    private final AlertSilenceService alertSilenceService;
    private final AlertNoticeDispatch alertNoticeDispatch;

    /**
     * 对告警组执行静默处理：匹配静默规则并判定时段，未静默则转交分发流程。
     *
     * <p>执行流程：
     * <ol>
     *   <li>加载启用的静默规则（缓存优先，回源数据库）；</li>
     *   <li>逐条规则判定：matchAll 直接命中，否则按 groupLabels 匹配；</li>
     *   <li>命中后按类型校验时段（一次性 / 周期性），命中静默时段即累计计数并提前返回；</li>
     *   <li>所有规则均未静默时，转入 {@link AlertNoticeDispatch#dispatchAlarm(AlertGroup)}。</li>
     * </ol>
     *
     * @param alertGroup 待静默处理的告警组
     */
    public void silenceAlarm(AlertGroup alertGroup) {
        // 空告警组直接跳过，避免下游空指针
        if (alertGroup == null) {
            return;
        }
        try {
            // 1. 加载启用的静默规则：缓存优先，未命中则回源数据库并回填
            List<AlertSilence> alertSilenceList = AlertCacheFactory.getAlertSilenceCache();
            if (alertSilenceList == null) {
                alertSilenceList = alertSilenceService.findAlertSilencesByEnableTrue();
                AlertCacheFactory.setAlertSilenceCache(alertSilenceList);
            }
            if (CollectionUtils.isEmpty(alertSilenceList)) {
                // 无静默规则直接转分发，避免无谓判定
                alertNoticeDispatch.dispatchAlarm(alertGroup);
                return;
            }
            // 2. 逐条规则判定是否静默当前告警组
            for (AlertSilence alertSilence : alertSilenceList) {
                boolean match = isMatchAll(alertSilence);
                // 非 matchAll 时按 groupLabels 与规则 labels 做匹配
                if (!match && alertGroup.getGroupLabels() != null) {
                    Map<String, String> ruleLabels = parseLabelMap(alertSilence.getLabels());
                    Map<String, String> alertLabels = alertGroup.getGroupLabels();
                    match = ruleLabels.entrySet().stream().anyMatch(item ->
                            alertLabels.containsKey(item.getKey())
                                    && item.getValue() != null
                                    && item.getValue().equals(alertLabels.get(item.getKey())));
                }
                if (!match) {
                    continue;
                }
                // 3. 命中规则后按类型校验时段，命中静默时段则累计计数并提前返回（即被静默）
                LocalDateTime now = LocalDateTime.now();
                Integer type = alertSilence.getType();
                if (type != null && type == TYPE_ONE_TIME) {
                    // 一次性静默：仅校验时段，未在时段内则继续下一条规则
                    if (!checkAndSave(now, alertSilence)) {
                        return;
                    }
                } else if (type != null && type == TYPE_CYCLIC) {
                    // 周期性静默：需同时满足生效星期与时段
                    int currentDayOfWeek = now.getDayOfWeek().getValue();
                    List<Integer> days = parseDayList(alertSilence.getDays());
                    if (days.contains(currentDayOfWeek) && !checkAndSave(now, alertSilence)) {
                        return;
                    }
                }
            }
            // 4. 所有规则均未静默，转入分发流程
            alertNoticeDispatch.dispatchAlarm(alertGroup);
        } catch (Exception e) {
            // 兜底：静默判定异常不应中断收敛链路，记录日志后放行至分发
            log.error("Silence alarm failed for group {}: {}", alertGroup.getGroupKey(), e.getMessage(), e);
            alertNoticeDispatch.dispatchAlarm(alertGroup);
        }
    }

    /**
     * 判定静默规则是否为「匹配所有告警」。
     *
     * @param alertSilence 静默规则
     * @return true 表示规则配置为 matchAll=1，对所有告警生效
     */
    private boolean isMatchAll(AlertSilence alertSilence) {
        return alertSilence.getMatchAll() != null && alertSilence.getMatchAll() == 1;
    }

    /**
     * 校验当前时间是否落在静默时段内，并在命中时累计静默计数并落库。
     *
     * <p>执行流程：
     * <ol>
     *   <li>周期性规则按 {@link LocalTime} 比较时段（支持跨天窗口）；</li>
     *   <li>一次性规则按 {@link LocalDateTime} 比较时段；</li>
     *   <li>命中时段则 {@code times+1} 并落库，返回 {@code false}（表示被静默）；</li>
     *   <li>未命中时段返回 {@code true}（表示不应静默，继续下一条规则）。</li>
     * </ol>
     *
     * @param now          当前时间
     * @param alertSilence 静默规则
     * @return true 表示不应静默（不在时段内）；false 表示已被静默
     */
    private boolean checkAndSave(LocalDateTime now, AlertSilence alertSilence) {
        boolean startMatch;
        boolean endMatch;
        Integer type = alertSilence.getType();
        if (type != null && type == TYPE_CYCLIC) {
            // 周期性规则：仅比较当天的时间部分，支持跨天窗口（如 22:00-06:00）
            LocalTime nowTime = now.toLocalTime();
            LocalTime startTime = parseTime(alertSilence.getPeriodStart());
            LocalTime endTime = parseTime(alertSilence.getPeriodEnd());
            if (startTime == null && endTime == null) {
                startMatch = true;
                endMatch = true;
            } else if (startTime == null) {
                startMatch = true;
                endMatch = !nowTime.isAfter(endTime);
            } else if (endTime == null) {
                startMatch = !nowTime.isBefore(startTime);
                endMatch = true;
            } else if (!startTime.isAfter(endTime)) {
                // 同一天内：[start, end]
                startMatch = !nowTime.isBefore(startTime);
                endMatch = !nowTime.isAfter(endTime);
            } else {
                // 跨天窗口：>=start OR <=end
                startMatch = !nowTime.isBefore(startTime) || !nowTime.isAfter(endTime);
                endMatch = true;
            }
        } else {
            // 一次性规则：按完整日期时间比较（periodStart/End 存储为 HH:mm:ss，需补当天日期）
            LocalTime startTime = parseTime(alertSilence.getPeriodStart());
            LocalTime endTime = parseTime(alertSilence.getPeriodEnd());
            startMatch = startTime == null || !now.toLocalTime().isBefore(startTime);
            endMatch = endTime == null || !now.toLocalTime().isAfter(endTime);
        }

        if (startMatch && endMatch) {
            // 命中静默时段：累计计数并落库，避免重复静默计数丢失
            int times = Optional.ofNullable(alertSilence.getTimes()).orElse(0);
            alertSilence.setTimes(times + 1);
            alertSilenceService.saveOrUpdate(alertSilence);
            return false;
        }
        return true;
    }

    /**
     * 将静默规则中 JSON String 形式的标签解析为 Map。
     *
     * @param json 标签 JSON 字符串
     * @return 标签 Map，空或异常返回空 map
     */
    private Map<String, String> parseLabelMap(String json) {
        if (StringUtils.isBlank(json)) {
            return Collections.emptyMap();
        }
        Map<String, String> result = JsonUtils.parseObject(json, new TypeReference<Map<String, String>>() {});
        return result == null ? Collections.emptyMap() : result;
    }

    /**
     * 将静默规则中 JSON String 形式的星期列表解析为 List。
     *
     * @param json 星期 JSON 数组字符串，如 {@code [1,2,3]}
     * @return 星期列表，空或异常返回空列表
     */
    private List<Integer> parseDayList(String json) {
        if (StringUtils.isBlank(json)) {
            return Collections.emptyList();
        }
        List<Integer> result = JsonUtils.parseArray(json, Integer.class);
        return result == null ? Collections.emptyList() : result;
    }

    /**
     * 将 {@code HH:mm:ss} 字符串解析为 {@link LocalTime}。
     *
     * @param timeStr 时间字符串
     * @return 解析后的 LocalTime，空串返回 null
     */
    private LocalTime parseTime(String timeStr) {
        if (StringUtils.isBlank(timeStr)) {
            return null;
        }
        return TimeUtils.parse(timeStr, TimeUtils.PATTERN_HMS);
    }
}
