package com.mhd.alert.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mhd.alert.cache.AlertCacheFactory;
import com.mhd.alert.entity.AlertEvent;
import com.mhd.alert.entity.AlertGroup;
import com.mhd.alert.entity.NoticeRule;
import com.mhd.alert.enums.MatchSpecificLabelEnum;
import com.mhd.alert.mapper.NoticeRuleMapper;
import com.mhd.alert.service.NoticeRuleService;
import com.mhd.boot.common.mybatis.core.wrapper.LambdaQueryWrapperX;
import com.mhd.boot.common.utils.SpringUtils;
import com.mhd.boot.common.utils.date.TimeUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 通知策略表Service实现类
 *
 * @author zhao-hao-dong
 */
@Service
public class NoticeRuleServiceImpl extends ServiceImpl<NoticeRuleMapper, NoticeRule>
        implements NoticeRuleService {

    @Override
    public NoticeRule getById(Long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public List<NoticeRule> getReceiverFilterRule(AlertGroup alert) {
        // 1. 从缓存中获取所有已启用的通知规则，避免每次调用都查询数据库
        List<NoticeRule> rules = AlertCacheFactory.getNoticeCache();
        // 2. 缓存穿透处理：如果缓存为空，则从数据库查询并回填缓存
        if (rules == null || rules.isEmpty()) {
            rules = SpringUtils.getAopProxy(this).findNoticeRulesByEnableTrue();
            AlertCacheFactory.setNoticeCache(rules);
        }
        // 3. 使用 Stream 流对所有规则进行过滤，只保留匹配当前告警的规则
        return rules.stream()
                .filter(rule -> {
                    // --- 第一步：标签过滤 ---
                    // 如果规则设置标签过滤”，则需要检查告警的标签是否匹配
                    if (rule.getMatchSpecificLabel() == MatchSpecificLabelEnum.MATCH_SPECIFIC.getCode()) {
                        if (rule.getLabels() != null && !rule.getLabels().isEmpty()) {
                            List<AlertEvent> alertEvents = alert.getAlerts();
                            // 核心逻辑：告警组中只要有任意一个子告警的标签完全匹配规则，即视为标签匹配成功
                            boolean labelMatch = alertEvents != null && alertEvents.stream().anyMatch(singleAlert -> {
                                Map<String, String> alertLabels = singleAlert.getLabels();
                                if (alertLabels == null) {
                                    return false;
                                }
                                return rule.getLabels().entrySet().stream().allMatch(labelItem ->
                                        Objects.equals(labelItem.getValue(), alertLabels.get(labelItem.getKey())));
                            });
                            // 如果标签不匹配，则此规则不适用，直接过滤掉
                            if (!labelMatch) {
                                return false;
                            }
                        }
                    }
                    // --- 第二步：星期过滤 ---
                    // 获取当前是星期几 (1-7, 1代表周一)
                    LocalDateTime nowDate = LocalDateTime.now();
                    int currentDayOfWeek = nowDate.toLocalDate().getDayOfWeek().getValue();
                    // 如果规则配置了生效的星期，则检查今天是否在配置范围内
                    if (rule.getDays() != null && !rule.getDays().isEmpty()) {
                        boolean dayMatch = rule.getDays().stream().anyMatch(item -> item == currentDayOfWeek);
                        // 如果今天不在配置的星期内，则此规则不适用
                        if (!dayMatch) {
                            return false;
                        }
                    }
                    // --- 第三步：时间段过滤 ---
                    // 获取当前时间，并解析规则配置的开始和结束时间
                    LocalTime nowTime = nowDate.toLocalTime();
                    LocalTime startTime = rule.getPeriodStart() == null
                            ? null : TimeUtils.parse(rule.getPeriodStart(), TimeUtils.PATTERN_HMS);
                    LocalTime endTime = rule.getPeriodEnd() == null
                            ? null : TimeUtils.parse(rule.getPeriodEnd(), TimeUtils.PATTERN_HMS);
                    // 如果开始和结束时间都未配置，表示全天生效，通过时间过滤
                    if (startTime == null && endTime == null) {
                        return true;
                    }
                    // 如果只配置了结束时间，判断当前时间是否早于或等于结束时间
                    if (startTime == null) {
                        return !nowTime.isAfter(endTime);
                    }
                    // 如果只配置了开始时间，判断当前时间是否晚于或等于开始时间
                    if (endTime == null) {
                        return !nowTime.isBefore(startTime);
                    }
                    // 如果同时配置了开始和结束时间，则分两种情况：
                    // 1. 同一天内 (如 09:00 - 18:00)：当前时间必须在 [开始时间, 结束时间] 区间内
                    if (!startTime.isAfter(endTime)) {
                        return !nowTime.isBefore(startTime) && !nowTime.isAfter(endTime);
                    }
                    // 2. 跨天 (如 22:00 - 06:00)：当前时间晚于开始时间 OR 早于结束时间
                    return !nowTime.isBefore(startTime) || !nowTime.isAfter(endTime);
                }).collect(Collectors.toList());
    }

    @Override
    public List<NoticeRule> findNoticeRulesByEnableTrue() {
        LambdaQueryWrapperX<NoticeRule> queryWrapper = new LambdaQueryWrapperX<>();
        queryWrapper.eq(NoticeRule::getEnable, 1);
        return list(queryWrapper);
    }
}




