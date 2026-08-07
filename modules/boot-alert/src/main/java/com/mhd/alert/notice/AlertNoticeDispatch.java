//package com.mhd.alert.notice;
//
//import com.google.common.collect.Maps;
//import com.mhd.alert.config.AlertThreadPoolConfig;
//import com.mhd.alert.entity.*;
//import com.mhd.alert.enums.AlertStatusEnum;
//import com.mhd.alert.enums.MatchSpecificLabelEnum;
//import com.mhd.alert.service.NoticeReceiverService;
//import com.mhd.alert.service.NoticeRuleService;
//import com.mhd.alert.service.NoticeTemplateService;
//import com.mhd.alert.store.AlertStoreHandler;
//import com.mhd.boot.common.sse.utils.SseMessageUtils;
//import com.mhd.boot.common.utils.collection.CollectionUtils;
//import com.mhd.boot.common.utils.json.JsonUtils;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDateTime;
//import java.util.*;
//import java.util.concurrent.RejectedExecutionException;
//import java.util.function.Function;
//import java.util.stream.Collectors;
//
///**
// * 告警通知分发
// *
// * @author zhao-hao-dong
// */
//@Component
//@Slf4j
//public class AlertNoticeDispatch {
//    private final AlertThreadPoolConfig alertThreadPoolConfig;
//    private final AlertStoreHandler alertStoreHandler;
//    private final NoticeTemplateService noticeTemplateService;
//    private final NoticeReceiverService noticeReceiverService;
//    private final NoticeRuleService noticeRuleService;
//    private final Map<Integer, AlertNoticeHandler> alertNoticeHandlerMap;
//
//    public AlertNoticeDispatch(AlertThreadPoolConfig alertThreadPoolConfig,
//                               AlertStoreHandler alertStoreHandler,
//                               NoticeTemplateService noticeTemplateService,
//                               NoticeReceiverService noticeReceiverService,
//                               NoticeRuleService noticeRuleService,
//                               List<AlertNoticeHandler> alertNoticeHandlerList) {
//        this.alertThreadPoolConfig = alertThreadPoolConfig;
//        this.alertStoreHandler = alertStoreHandler;
//        this.noticeTemplateService = noticeTemplateService;
//        this.noticeReceiverService = noticeReceiverService;
//        this.noticeRuleService = noticeRuleService;
//        this.alertNoticeHandlerMap = Maps.newHashMapWithExpectedSize(alertNoticeHandlerList.size());
//        alertNoticeHandlerList.forEach(r -> {
//            if (r.type() == null) throw new NullPointerException("AlertNoticeHandler type is null");
//            alertNoticeHandlerMap.put(r.type().getCode(), r);
//        });
//    }
//
//    /**
//     * 发送单条通知消息
//     *
//     * @param receiver       通知接收人信息
//     * @param noticeTemplate 通知模板，如果为空则会尝试获取默认模板
//     * @param alert          告警信息组
//     * @return 是否成功发送
//     */
//    public boolean sendNoticeMsg(NoticeReceiver receiver, NoticeTemplate noticeTemplate, AlertGroup alert) {
//        if (receiver == null || receiver.getType() == null) {
//            log.warn("DispatcherAlarm-sendNoticeMsg params is empty alert:[{}], receiver:[{}]", alert, receiver);
//            return false;
//        }
//        int type = receiver.getType();
//        // 根据接收人类型找到对应的通知处理器
//        if (alertNoticeHandlerMap.containsKey(type)) {
//            AlertNoticeHandler alertNoticeHandler = alertNoticeHandlerMap.get(type);
//            // 如果未指定模板，则尝试获取该通知类型的默认模板
//            if (noticeTemplate == null) {
//                noticeTemplate = noticeTemplateService.getDefaultNoticeTemplateByType(alertNoticeHandler.type().getCode());
//            }
//            // 如果仍然没有模板（且不是短信类型），则抛出异常
//            if (noticeTemplate == null && alertNoticeHandler.type().getCode() != AlertNoticeTypeEnum.SMS.getCode()) {
//                log.error("alert does not have mapping default notice template. type: {}.", alertNoticeHandler.type());
//                throw new NullPointerException(alertNoticeHandler.type().getDescription() + " does not have mapping default notice template");
//            }
//            // 调用具体的处理器发送通知
//            alertNoticeHandler.send(receiver, noticeTemplate, alert);
//            return true;
//        }
//        return false;
//    }
//
//    /**
//     * 分发告警的入口方法
//     * 1. 持久化告警组
//     * 2. 触发通知发送流程
//     *
//     * @param alertGroup 待分发的告警组
//     */
//    public void dispatchAlarm(AlertGroup alertGroup) {
//        if (alertGroup != null) {
//            // 1. 将告警组存入数据库或缓存
//            AlertGroup storedAlertGroup = alertStoreHandler.store(alertGroup);
//            // 2. 开始发送通知
//            sendNotify(storedAlertGroup);
//            // 3. 发送告警到 SSE 客户端
//            SseMessageUtils.publishAll(JsonUtils.toJsonString(storedAlertGroup));
//        }
//    }
//
//    /**
//     * 根据规则将告警发送给具体的接收人
//     * 这是一个核心流程方法，负责协调规则、模板、接收人和告警内容。
//     *
//     * @param alertGroup 经过持久化处理的告警组
//     */
//    private void sendNotify(AlertGroup alertGroup) {
//        // 1. 获取适用于当前告警的所有通知规则
//        List<NoticeRule> receiverFilterRules = noticeRuleService.getReceiverFilterRule(alertGroup);
//        if (CollectionUtils.isEmpty(receiverFilterRules)) return;
//        for (NoticeRule rule : receiverFilterRules) {
//            // 2. 对每一条规则，获取对应的通知模板
//            NoticeTemplate noticeTemplate = noticeTemplateService.selectById(rule.getTemplateId());
//            // 3. 根据规则裁剪告警信息，只保留与该规则相关的告警
//            AlertGroup noticeAlert = scopeAlertToRule(alertGroup, rule);
//            // 4. 获取规则中配置的所有接收人 ID，并查询接收人详情
//            for (Long receiverId : rule.getReceiverId()) {
//                NoticeReceiver receiver = noticeReceiverService.selectById(receiverId);
//                if (receiver == null || receiver.getType() == null) {
//                    log.warn("DispatcherAlarm-sendNotify skip invalid receiver, receiverId: {}, alertId: {}", receiverId, alertGroup.getId());
//                    continue;
//                }
//                // 5. 发送通知消息
//                try {
//                    alertThreadPoolConfig.executeNotify(() -> {
//                        try {
//                            sendNoticeMsg(receiver, noticeTemplate, noticeAlert);
//                        } catch (AlertNoticeException e) {
//                            log.warn("DispatchTask sendNoticeMsg error, message: {}", e.getMessage());
//                        }
//                    });
//                } catch (RejectedExecutionException e) {
//                    log.warn("DispatchTask rejected notify task, receiverId: {}, type: {}, message: {}",
//                            receiverId, receiver.getType(), e.getMessage());
//                }
//            }
//        }
//    }
//
//    /**
//     * 根据通知规则，裁剪告警组信息
//     * 此方法会过滤出告警组中与规则标签匹配的告警，并重新计算告警组的公共属性（如 commonLabels, groupKey 等）。
//     *
//     * @param alert 原始告警组
//     * @param rule  通知规则
//     * @return 裁剪后的新告警组
//     */
//    private AlertGroup scopeAlertToRule(AlertGroup alert, NoticeRule rule) {
//        // 如果规则是“跳过所有标签匹配”或规则/告警本身没有标签，则直接返回原始告警组
//        if (rule.getMatchSpecificLabel() == MatchSpecificLabelEnum.SKIP_ALL.getCode() || rule.getLabels() == null || rule.getLabels().isEmpty()
//                || alert.getAlerts() == null) {
//            return alert;
//        }
//        // 1. 过滤出与规则标签完全匹配的告警
//        List<AlertSingle> matchingAlerts = alert.getAlerts().stream()
//                .filter(singleAlert -> singleAlert.getLabels() != null
//                        && rule.getLabels().entrySet().stream().allMatch(label ->
//                        Objects.equals(label.getValue(), singleAlert.getLabels().get(label.getKey()))))
//                .toList();
//
//        // 2. 从匹配的告警中提取公共的 Labels 和 Annotations
//        Map<String, String> commonLabels = extractCommonAttributes(matchingAlerts, AlertSingle::getLabels);
//        Map<String, String> commonAnnotations =
//                extractCommonAttributes(matchingAlerts, AlertSingle::getAnnotations);
//
//        // 3. 基于公共 Labels 重新构建 GroupLabels
//        Map<String, String> groupLabels = extractGroupLabels(alert.getGroupLabels(), commonLabels);
//
//        // 4. 如果 GroupLabels 发生变化，则需要生成新的 GroupKey
//        String groupKey = Objects.equals(groupLabels, alert.getGroupLabels())
//                ? alert.getGroupKey() : generateGroupKey(groupLabels);
//
//        // 5. 使用构建者模式创建一个新的、裁剪后的 AlertGroup 对象
//        return AlertGroup.builder()
//                .id(alert.getId())
//                .groupKey(groupKey)
//                .status(determineGroupStatus(matchingAlerts))
//                .groupLabels(groupLabels)
//                .commonLabels(commonLabels)
//                .commonAnnotations(commonAnnotations)
//                .alertFingerprints(matchingAlerts.stream()
//                        .map(AlertSingle::getFingerprint)
//                        .filter(Objects::nonNull)
//                        .toList())
//                .createBy(alert.getCreateBy())
//                .createTime(firstTime(matchingAlerts, AlertSingle::getCreateTime, alert.getCreateTime()))
//                .updateBy(alert.getUpdateBy())
//                .updateTime(lastTime(matchingAlerts, AlertSingle::getUpdateTime, alert.getUpdateTime()))
//                .alerts(matchingAlerts)
//                .build();
//    }
//
//    /**
//     * 从一组告警中提取公共属性（Labels 或 Annotations）
//     * 公共属性是指在所有告警中都存在且值相同的键值对。
//     *
//     * @param alerts     告警列表
//     * @param attributes 获取属性的函数，例如 AlertSingle::getLabels
//     * @return 包含公共属性的 Map
//     */
//    private Map<String, String> extractCommonAttributes(
//            Collection<AlertSingle> alerts,
//            Function<AlertSingle, Map<String, String>> attributes) {
//        if (alerts.isEmpty()) {
//            return new HashMap<>(0);
//        }
//        Map<String, String> firstAttributes = attributes.apply(alerts.iterator().next());
//        Map<String, String> common =
//                firstAttributes == null ? new HashMap<>(0) : new HashMap<>(firstAttributes);
//        for (AlertSingle alert : alerts) {
//            Map<String, String> current = attributes.apply(alert);
//            common.keySet().removeIf(key ->
//                    current == null || !current.containsKey(key)
//                            || !Objects.equals(common.get(key), current.get(key)));
//        }
//        return common;
//    }
//
//    /**
//     * 从原始 GroupLabels 中提取出存在于公共 Labels 中的部分
//     *
//     * @param originalGroupLabels 原始的分组标签
//     * @param commonLabels        计算出的公共标签
//     * @return 新的分组标签
//     */
//    private Map<String, String> extractGroupLabels(
//            Map<String, String> originalGroupLabels,
//            Map<String, String> commonLabels) {
//        Map<String, String> groupLabels = new HashMap<>();
//        if (originalGroupLabels != null) {
//            originalGroupLabels.keySet().forEach(key -> {
//                if (commonLabels.containsKey(key)) {
//                    groupLabels.put(key, commonLabels.get(key));
//                }
//            });
//        }
//        return groupLabels;
//    }
//
//    /**
//     * 根据分组标签生成唯一的 GroupKey
//     * 通过对键值对排序并拼接，确保相同的标签组合生成相同的 Key。
//     *
//     * @param groupLabels 分组标签
//     * @return 生成的 GroupKey
//     */
//    private String generateGroupKey(Map<String, String> groupLabels) {
//        return groupLabels.entrySet().stream()
//                .sorted(Map.Entry.comparingByKey())
//                .map(entry -> entry.getKey() + ":" + entry.getValue())
//                .collect(Collectors.joining(","));
//    }
//
//    /**
//     * 根据告警列表确定告警组的状态
//     * 只要有一个告警是 FIRING（触发中），整个组的状态就是 FIRING。
//     *
//     * @param alerts 告警列表
//     * @return 告警组状态码
//     */
//    private String determineGroupStatus(List<AlertSingle> alerts) {
//        return alerts.stream().anyMatch(alert ->
//                AlertStatusEnum.FIRING.getCode().equals(alert.getStatus()))
//                ? AlertStatusEnum.FIRING.getCode() : AlertStatusEnum.RESOLVED.getCode();
//    }
//
//    /**
//     * 获取告警列表中的最早时间
//     *
//     * @param alerts   告警列表
//     * @param time     获取时间的函数
//     * @param fallback 如果列表为空，返回的默认时间
//     * @return 最早的时间
//     */
//    private LocalDateTime firstTime(
//            List<AlertSingle> alerts,
//            Function<AlertSingle, LocalDateTime> time,
//            LocalDateTime fallback) {
//        return alerts.stream().map(time).filter(Objects::nonNull)
//                .min(LocalDateTime::compareTo).orElse(fallback);
//    }
//
//    /**
//     * 获取告警列表中的最晚时间
//     *
//     * @param alerts   告警列表
//     * @param time     获取时间的函数
//     * @param fallback 如果列表为空，返回的默认时间
//     * @return 最晚的时间
//     */
//    private LocalDateTime lastTime(
//            List<AlertSingle> alerts,
//            Function<AlertSingle, LocalDateTime> time,
//            LocalDateTime fallback) {
//        return alerts.stream().map(time).filter(Objects::nonNull)
//                .max(LocalDateTime::compareTo).orElse(fallback);
//    }
//}
