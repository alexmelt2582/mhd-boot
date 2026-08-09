package com.mhd.alert.reduce;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mhd.alert.entity.AlertEvent;
import com.mhd.alert.entity.AlertGroup;
import com.mhd.alert.entity.AlertInhibit;
import com.mhd.alert.enums.AlertStatusEnum;
import com.mhd.alert.mapper.AlertInhibitMapper;
import com.mhd.boot.common.utils.json.JsonUtils;
import com.mhd.boot.common.utils.collection.CollectionUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 告警抑制收敛器。
 *
 * <p>参照 Prometheus 告警抑制机制：当「源告警」触发时，与之匹配「等同标签」的
 * 「目标告警」将被抑制（不下发通知）。典型场景——critical 告警触发时抑制同实例的 warning 告警。
 *
 * <p>执行链路位置：{@link AlarmGroupReduce} → <b>此处</b> → {@link AlarmSilenceReduce}。
 *
 * <p>源告警采用内存缓存 + TTL（默认 4 小时）+ 定时清理（60s）的方式管理，
 * 避免源告警长期驻留导致目标告警被永久抑制。抑制规则在启动时从数据库加载并缓存，
 * 后续可通过 {@link #refreshInhibitRules(List)} 刷新。
 *
 * @author zhao-hao-dong
 */
@Component
@Slf4j
public class AlarmInhibitReduce implements DisposableBean {

    /**
     * 源告警缓存清理周期（毫秒）
     */
    private static final long CLEANUP_INTERVAL_MS = 60_000L;

    /**
     * 源告警默认存活时长：4 小时（毫秒）
     */
    private static final long DEFAULT_SOURCE_ALERT_TTL_MS = 4 * 60 * 60 * 1000L;

    private final AlarmSilenceReduce alarmSilenceReduce;
    private final AlertInhibitMapper alertInhibitMapper;

    /**
     * 抑制规则缓存：ruleId → 规则
     */
    private final Map<Long, AlertInhibit> inhibitRules;

    /**
     * 源告警缓存：ruleId → (fingerprint → 源告警条目)
     */
    private final Map<Long, Map<String, SourceAlertEntry>> sourceAlertCache;

    private final long sourceAlertTtlMs;

    private final ScheduledExecutorService cleanupScheduler;

    /**
     * 构造抑制收敛器，初始化规则缓存与定时清理任务。
     *
     * @param alarmSilenceReduce 下游静默收敛器
     * @param alertInhibitMapper 抑制规则 Mapper
     */
    public AlarmInhibitReduce(AlarmSilenceReduce alarmSilenceReduce, AlertInhibitMapper alertInhibitMapper) {
        this.alarmSilenceReduce = alarmSilenceReduce;
        this.alertInhibitMapper = alertInhibitMapper;
        this.sourceAlertTtlMs = DEFAULT_SOURCE_ALERT_TTL_MS;
        this.inhibitRules = new ConcurrentHashMap<>(8);
        this.sourceAlertCache = new ConcurrentHashMap<>(8);
        this.cleanupScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "inhibit-clean-up");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * 启动后加载启用的抑制规则并开启定时清理。
     */
    @PostConstruct
    public void init() {
        // 1. 从数据库加载启用的抑制规则并缓存
        refreshInhibitRules(loadEnabledInhibitRules());
        // 2. 开启源告警缓存定时清理，避免过期源告警长期抑制目标告警
        cleanupScheduler.scheduleAtFixedRate(this::runCleanup, CLEANUP_INTERVAL_MS, CLEANUP_INTERVAL_MS,
                TimeUnit.MILLISECONDS);
    }

    /**
     * 重新加载启用的抑制规则列表。
     *
     * @return 启用的抑制规则集合
     */
    private List<AlertInhibit> loadEnabledInhibitRules() {
        QueryWrapper<AlertInhibit> wrapper = new QueryWrapper<>();
        wrapper.eq("enable", 1);
        List<AlertInhibit> rules = alertInhibitMapper.selectList(wrapper);
        return rules == null ? Collections.emptyList() : rules;
    }

    /**
     * 刷新抑制规则缓存（供规则变更后调用）。
     *
     * @param rules 最新的抑制规则列表，null 时仅清空缓存
     */
    public void refreshInhibitRules(List<AlertInhibit> rules) {
        this.inhibitRules.clear();
        if (rules == null) {
            return;
        }
        for (AlertInhibit rule : rules) {
            this.inhibitRules.put(rule.getId(), rule);
        }
    }

    /**
     * 对告警组执行抑制处理：缓存命中的源告警，过滤被抑制的目标告警，剩余告警转入静默流程。
     *
     * <p>执行流程：
     * <ol>
     *   <li>空告警组或无抑制规则时直接转入静默流程，避免无谓计算；</li>
     *   <li>遍历组内告警，将匹配某规则源标签的 FIRING 告警缓存为源告警；</li>
     *   <li>剔除命中抑制条件的目标告警（被源告警按等同标签抑制）；</li>
     *   <li>组内仍有剩余告警时，转入 {@link AlarmSilenceReduce#silenceAlarm(AlertGroup)}。</li>
     * </ol>
     *
     * @param alertGroup 待抑制处理的告警组
     */
    public void inhibitAlarm(AlertGroup alertGroup) {
        if (alertGroup == null) {
            return;
        }
        try {
            // 1. 无抑制规则时直接转入静默流程，避免无谓计算
            if (inhibitRules.isEmpty() || CollectionUtils.isEmpty(alertGroup.getAlerts())) {
                alarmSilenceReduce.silenceAlarm(alertGroup);
                return;
            }
            // 2. 遍历组内告警，将匹配某规则源标签的 FIRING 告警缓存为源告警
            for (AlertEvent alert : alertGroup.getAlerts()) {
                for (AlertInhibit rule : inhibitRules.values()) {
                    if (isSourceAlert(alert, rule)) {
                        cacheSourceAlert(alert, rule);
                    }
                }
            }
            // 3. 剔除命中抑制条件的目标告警（被源告警按等同标签抑制）
            alertGroup.getAlerts().removeIf(this::shouldInhibit);
            // 4. 组内仍有剩余告警时，转入静默流程
            if (!alertGroup.getAlerts().isEmpty()) {
                alarmSilenceReduce.silenceAlarm(alertGroup);
            }
        } catch (Exception e) {
            // 兜底：抑制异常不应中断整条收敛链路，记录日志后放行
            log.error("Inhibit alarm failed for group {}: {}", alertGroup.getGroupKey(), e.getMessage(), e);
            alarmSilenceReduce.silenceAlarm(alertGroup);
        }
    }

    /**
     * 判断告警是否为某抑制规则的源告警：状态为 FIRING 且标签完全匹配源标签。
     *
     * @param alert 待判定的告警
     * @param rule  抑制规则
     * @return true 表示该告警是源告警，需缓存
     */
    private boolean isSourceAlert(AlertEvent alert, AlertInhibit rule) {
        if (alert == null || rule == null) {
            return false;
        }
        // 仅 FIRING 状态的告警作为抑制源，resolved 不再抑制其他告警
        if (!AlertStatusEnum.FIRING.getCode().equals(alert.getStatus())) {
            return false;
        }
        return matchLabels(alert.getLabels(), parseLabelMap(rule.getSourceLabels()));
    }

    /**
     * 判断告警是否应被抑制：匹配某规则目标标签，且存在按等同标签匹配的活跃源告警。
     *
     * @param alert 待判定的告警
     * @return true 表示该告警应被抑制（不下发）
     */
    private boolean shouldInhibit(AlertEvent alert) {
        if (alert == null) {
            return false;
        }
        // resolved 告警不参与抑制，确保恢复通知能正常下发
        if (AlertStatusEnum.RESOLVED.getCode().equals(alert.getStatus())) {
            return false;
        }
        for (AlertInhibit rule : inhibitRules.values()) {
            if (!matchLabels(alert.getLabels(), parseLabelMap(rule.getTargetLabels()))) {
                continue;
            }
            List<AlertEvent> sourceAlerts = getActiveSourceAlerts(rule);
            if (sourceAlerts.isEmpty()) {
                continue;
            }
            List<String> equalLabels = parseStringList(rule.getEqualLabels());
            for (AlertEvent source : sourceAlerts) {
                if (matchEqualLabels(source, alert, equalLabels)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断告警标签是否完全包含且等于所需标签。
     *
     * @param alertLabels    告警标签
     * @param requiredLabels 规则要求匹配的标签
     * @return true 表示告警标签覆盖并匹配所有所需标签
     */
    private boolean matchLabels(Map<String, String> alertLabels, Map<String, String> requiredLabels) {
        if (requiredLabels == null || requiredLabels.isEmpty()) {
            return false;
        }
        if (alertLabels == null) {
            return false;
        }
        return requiredLabels.entrySet().stream()
                .allMatch(e -> e.getValue() != null && e.getValue().equals(alertLabels.get(e.getKey())));
    }

    /**
     * 判断源告警与目标告警在指定等同标签上的取值是否全部一致。
     *
     * @param source     源告警
     * @param target     目标告警
     * @param equalLabels 需要相等的标签键列表
     * @return true 表示等同标签全部一致，目标告警应被抑制
     */
    private boolean matchEqualLabels(AlertEvent source, AlertEvent target, List<String> equalLabels) {
        if (equalLabels == null || equalLabels.isEmpty()) {
            return true;
        }
        Map<String, String> sourceLabels = source.getLabels();
        Map<String, String> targetLabels = target.getLabels();
        return equalLabels.stream().allMatch(label -> {
            String sv = sourceLabels == null ? null : sourceLabels.get(label);
            String tv = targetLabels == null ? null : targetLabels.get(label);
            return sv != null && sv.equals(tv);
        });
    }

    /**
     * 缓存源告警，并以指纹为键去重，过期条目即时清理。
     *
     * @param alert 源告警
     * @param rule  命中的抑制规则
     */
    private void cacheSourceAlert(AlertEvent alert, AlertInhibit rule) {
        Map<String, SourceAlertEntry> ruleCache = sourceAlertCache.computeIfAbsent(
                rule.getId(), k -> new ConcurrentHashMap<>());
        long now = System.currentTimeMillis();
        ruleCache.put(alert.getFingerprint(),
                new SourceAlertEntry(alert, now, now + sourceAlertTtlMs));
        cleanupExpiredEntries(ruleCache);
    }

    /**
     * 获取某规则下未过期的活跃源告警列表。
     *
     * @param rule 抑制规则
     * @return 活跃源告警列表，无则返回空列表
     */
    private List<AlertEvent> getActiveSourceAlerts(AlertInhibit rule) {
        Map<String, SourceAlertEntry> ruleCache = sourceAlertCache.get(rule.getId());
        if (ruleCache == null || ruleCache.isEmpty()) {
            return Collections.emptyList();
        }
        long now = System.currentTimeMillis();
        List<AlertEvent> active = new ArrayList<>(ruleCache.size());
        for (SourceAlertEntry entry : ruleCache.values()) {
            if (entry.expiryTime > now) {
                active.add(entry.alert);
            }
        }
        return active;
    }

    /**
     * 定时清理任务：遍历所有规则的源告警缓存，剔除过期条目，回收空缓存。
     */
    private void runCleanup() {
        try {
            sourceAlertCache.values().forEach(this::cleanupExpiredEntries);
            // 回收已无条目的规则缓存，避免内存泄漏
            sourceAlertCache.entrySet().removeIf(e -> e.getValue().isEmpty());
        } catch (Exception e) {
            log.error("Inhibit source alert cache cleanup failed: {}", e.getMessage(), e);
        }
    }

    /**
     * 剔除单条规则缓存中的过期源告警条目。
     *
     * @param cache 单条规则的源告警缓存
     */
    private void cleanupExpiredEntries(Map<String, SourceAlertEntry> cache) {
        if (cache == null) {
            return;
        }
        long now = System.currentTimeMillis();
        cache.entrySet().removeIf(e -> e.getValue().expiryTime <= now);
    }

    /**
     * 将抑制规则中 JSON String 形式的标签解析为 Map。
     *
     * @param json 标签 JSON 字符串
     * @return 标签 Map，空或异常返回空 map
     */
    private Map<String, String> parseLabelMap(String json) {
        if (json == null || json.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> result = JsonUtils.parseObject(json, new TypeReference<Map<String, String>>() {});
        return result == null ? Collections.emptyMap() : result;
    }

    /**
     * 将抑制规则中 JSON String 形式的等同标签列表解析为 List。
     *
     * @param json 等同标签 JSON 数组字符串
     * @return 标签键列表，空或异常返回空列表
     */
    private List<String> parseStringList(String json) {
        if (json == null || json.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> result = JsonUtils.parseArray(json, String.class);
        return result == null ? Collections.emptyList() : result;
    }

    /**
     * 关闭定时清理线程池，避免 Bean 销毁后线程泄漏。
     */
    @Override
    public void destroy() {
        cleanupScheduler.shutdownNow();
    }

    /**
     * 源告警缓存条目，记录告警对象与过期时间。
     */
    private record SourceAlertEntry(AlertEvent alert, long createTime, long expiryTime) {
    }
}
