package com.mhd.alert.notice;

import com.mhd.alert.config.AlertThreadPoolConfig;
import com.mhd.alert.entity.AlertEvent;
import com.mhd.alert.entity.AlertGroup;
import com.mhd.alert.entity.NoticeReceiver;
import com.mhd.alert.entity.NoticeRule;
import com.mhd.alert.entity.NoticeTemplate;
import com.mhd.alert.enums.AlertStatusEnum;
import com.mhd.alert.enums.MatchSpecificLabelEnum;
import com.mhd.alert.service.NoticeReceiverService;
import com.mhd.alert.service.NoticeRuleService;
import com.mhd.alert.service.NoticeTemplateService;
import com.mhd.alert.store.AlertStoreHandler;
import com.mhd.boot.common.sse.utils.SseMessageUtils;
import com.mhd.boot.common.utils.collection.CollectionUtils;
import com.mhd.boot.common.utils.json.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 告警通知分发器。
 *
 * <p>位于收敛链路末端，承接 {@code AlarmSilenceReduce} 未静默的告警组，完成三件事：
 * <ol>
 *   <li>持久化告警组及其明细告警（去重合并历史状态）；</li>
 *   <li>按通知规则匹配接收人并异步派发通知；</li>
 *   <li>通过 SSE 向前端实时推送告警组。</li>
 * </ol>
 *
 * <p>通知派发采用线程池异步执行（{@link AlertThreadPoolConfig#executeNotify(Runnable)}），
 * 单个接收人发送异常不会阻塞其他接收人；线程池满时记录告警并丢弃任务，避免反压阻塞收敛链路。
 *
 * <p>通知处理器通过构造器注入 {@code List<AlertNoticeHandler>} 并按 {@link AlertNoticeTypeEnum#getCode()}
 * 建立索引，运行时按 {@link NoticeReceiver#getType()} 路由到对应处理器。
 *
 * @author zhao-hao-dong
 */
@Component
@Slf4j
public class AlertNoticeDispatch {

    private final AlertThreadPoolConfig alertThreadPoolConfig;
    private final AlertStoreHandler alertStoreHandler;
    private final NoticeTemplateService noticeTemplateService;
    private final NoticeReceiverService noticeReceiverService;
    private final NoticeRuleService noticeRuleService;
    /**
     * 通知处理器索引：receiver.type → 处理器实例
     */
    private final Map<Integer, AlertNoticeHandler> alertNoticeHandlerMap;

    /**
     * 构造分发器，按 {@link AlertNoticeHandler#type()} 建立通知处理器索引。
     *
     * @param alertThreadPoolConfig    告警线程池配置，用于异步派发通知
     * @param alertStoreHandler        告警持久化处理器
     * @param noticeTemplateService    通知模板服务
     * @param noticeReceiverService    通知接收人服务
     * @param noticeRuleService        通知规则服务
     * @param alertNoticeHandlerList   Spring 注入的全部通知处理器
     */
    public AlertNoticeDispatch(AlertThreadPoolConfig alertThreadPoolConfig,
                               AlertStoreHandler alertStoreHandler,
                               NoticeTemplateService noticeTemplateService,
                               NoticeReceiverService noticeReceiverService,
                               NoticeRuleService noticeRuleService,
                               List<AlertNoticeHandler> alertNoticeHandlerList) {
        this.alertThreadPoolConfig = alertThreadPoolConfig;
        this.alertStoreHandler = alertStoreHandler;
        this.noticeTemplateService = noticeTemplateService;
        this.noticeReceiverService = noticeReceiverService;
        this.noticeRuleService = noticeRuleService;
        this.alertNoticeHandlerMap = new HashMap<>(alertNoticeHandlerList.size());
        for (AlertNoticeHandler handler : alertNoticeHandlerList) {
            if (handler.type() == null) {
                throw new NullPointerException("AlertNoticeHandler type is null: " + handler.getClass().getName());
            }
            this.alertNoticeHandlerMap.put(handler.type().getCode(), handler);
        }
    }

    /**
     * 向单个接收人发送通知：按接收人类型路由到对应通知处理器。
     *
     * <p>执行流程：
     * <ol>
     *   <li>空接收人或类型为空时直接返回 false，避免无效派发；</li>
     *   <li>按 type 路由到通知处理器，未注册类型返回 false；</li>
     *   <li>模板为空时回退到该类型的默认预置模板；</li>
     *   <li>非 SMS 类型仍无模板时抛 NPE，保证模板完整性约束；</li>
     *   <li>调用处理器 {@link AlertNoticeHandler#send} 完成实际发送。</li>
     * </ol>
     *
     * @param receiver       通知接收人
     * @param noticeTemplate 通知模板，为空时尝试获取默认模板
     * @param alert          告警组
     * @return true 表示已派发至处理器；false 表示接收人无效或类型未注册
     */
    public boolean sendNoticeMsg(NoticeReceiver receiver, NoticeTemplate noticeTemplate, AlertGroup alert) {
        // 1. 空接收人或类型为空直接返回，避免无效派发
        if (receiver == null || receiver.getType() == null) {
            log.warn("DispatcherAlarm-sendNoticeMsg params is empty alert:[{}], receiver:[{}]", alert, receiver);
            return false;
        }
        int type = receiver.getType();
        // 2. 按接收人类型路由到通知处理器
        if (!alertNoticeHandlerMap.containsKey(type)) {
            return false;
        }
        AlertNoticeHandler alertNoticeHandler = alertNoticeHandlerMap.get(type);
        // 3. 模板为空时回退到该类型的默认预置模板
        if (noticeTemplate == null) {
            noticeTemplate = noticeTemplateService.getDefaultNoticeTemplateByType(alertNoticeHandler.type().getCode());
        }
        // 4. 非 SMS 类型仍无模板时抛 NPE，保证模板完整性约束（SMS 无需模板）
        if (noticeTemplate == null && alertNoticeHandler.type().getCode() != AlertNoticeTypeEnum.SMS.getCode()) {
            log.error("alert does not have mapping default notice template. type: {}.", alertNoticeHandler.type());
            throw new NullPointerException(alertNoticeHandler.type().getDescription() + " does not have mapping default notice template");
        }
        // 5. 调用处理器完成实际发送
        alertNoticeHandler.send(receiver, noticeTemplate, alert);
        return true;
    }

    /**
     * 分发告警组入口：持久化 → 通知派发 → SSE 推送。
     *
     * <p>执行流程：
     * <ol>
     *   <li>空告警组直接跳过；</li>
     *   <li>委托 {@link AlertStoreHandler#store} 持久化告警组及其明细告警；</li>
     *   <li>触发通知派发 {@link #sendNotify(AlertGroup)}；</li>
     *   <li>通过 SSE 向前端广播持久化后的告警组 JSON。</li>
     * </ol>
     *
     * @param alertGroup 待分发的告警组
     */
    public void dispatchAlarm(AlertGroup alertGroup) {
        // 1. 空告警组直接跳过，避免空指针
        if (alertGroup == null) {
            return;
        }
        // 2. 持久化告警组及其明细告警，并合并历史状态
        AlertGroup storedAlertGroup = alertStoreHandler.store(alertGroup);
        // 3. 触发通知派发：匹配规则、按接收人异步发送
        sendNotify(storedAlertGroup);
        // 4. 通过 SSE 向前端广播持久化后的告警组，保证前端看到的是最新合并状态
        SseMessageUtils.publishAll(JsonUtils.toJsonString(storedAlertGroup));
    }

    /**
     * 按通知规则匹配接收人并异步派发通知。
     *
     * <p>执行流程：
     * <ol>
     *   <li>查询适用于当前告警组的通知规则列表，空则直接返回；</li>
     *   <li>逐条规则：加载通知模板、裁剪告警组到规则作用域；</li>
     *   <li>遍历规则配置的接收人，跳过无效接收人；</li>
     *   <li>异步派发通知任务，捕获 {@link AlertNoticeException} 避免中断；</li>
     *   <li>线程池满时记录告警并丢弃任务，避免反压阻塞收敛链路。</li>
     * </ol>
     *
     * @param alertGroup 已持久化的告警组
     */
    private void sendNotify(AlertGroup alertGroup) {
        // 1. 查询适用于当前告警组的通知规则，无规则则直接返回
        List<NoticeRule> receiverFilterRules = noticeRuleService.getReceiverFilterRule(alertGroup);
        if (CollectionUtils.isEmpty(receiverFilterRules)) {
            return;
        }
        for (NoticeRule rule : receiverFilterRules) {
            // 2. 加载规则绑定的通知模板，并按规则标签裁剪告警组到该规则作用域
            NoticeTemplate noticeTemplate = noticeTemplateService.selectById(rule.getTemplateId());
            AlertGroup noticeAlert = scopeAlertToRule(alertGroup, rule);
            List<Long> receiverIds = rule.getReceiverId();
            if (CollectionUtils.isEmpty(receiverIds)) {
                continue;
            }
            // 3. 遍历规则配置的接收人，逐个派发
            for (Long receiverId : receiverIds) {
                NoticeReceiver receiver = noticeReceiverService.selectById(receiverId);
                if (receiver == null || receiver.getType() == null) {
                    log.warn("DispatcherAlarm-sendNotify skip invalid receiver, receiverId: {}, alertId: {}", receiverId, alertGroup.getId());
                    continue;
                }
                // 4. 异步派发，单接收人异常不影响其他接收人
                try {
                    alertThreadPoolConfig.executeNotify(() -> {
                        try {
                            sendNoticeMsg(receiver, noticeTemplate, noticeAlert);
                        } catch (AlertNoticeException e) {
                            log.warn("DispatchTask sendNoticeMsg error, message: {}", e.getMessage());
                        }
                    });
                } catch (RejectedExecutionException e) {
                    // 5. 线程池满：记录告警并丢弃任务，避免反压阻塞收敛链路
                    log.warn("DispatchTask rejected notify task, receiverId: {}, type: {}, message: {}",
                            receiverId, receiver.getType(), e.getMessage());
                }
            }
        }
    }

    /**
     * 按通知规则裁剪告警组：仅保留与规则标签匹配的告警，并重算公共属性。
     *
     * <p>执行流程：
     * <ol>
     *   <li>规则跳过标签匹配或无标签、告警组无告警时，直接返回原告警组；</li>
     *   <li>过滤出与规则标签完全匹配的告警列表；</li>
     *   <li>重算公共标签、公共注解与分组标签；</li>
     *   <li>分组标签变化时重算 groupKey，否则沿用原值；</li>
     *   <li>组装裁剪后的 {@link AlertGroup} 返回。</li>
     * </ol>
     *
     * @param alert 原始告警组
     * @param rule  通知规则
     * @return 裁剪后的新告警组；规则无需裁剪时返回原对象
     */
    private AlertGroup scopeAlertToRule(AlertGroup alert, NoticeRule rule) {
        // 1. 跳过标签匹配 / 规则无标签 / 告警组无告警时直接返回原对象
        if (Objects.equals(rule.getMatchSpecificLabel(), MatchSpecificLabelEnum.SKIP_ALL.getCode())
                || rule.getLabels() == null || rule.getLabels().isEmpty()
                || alert.getAlerts() == null) {
            return alert;
        }
        // 2. 过滤出与规则标签完全匹配的告警
        List<AlertEvent> matchingAlerts = alert.getAlerts().stream()
                .filter(singleAlert -> singleAlert.getLabels() != null
                        && rule.getLabels().entrySet().stream().allMatch(label ->
                        Objects.equals(label.getValue(), singleAlert.getLabels().get(label.getKey()))))
                .collect(Collectors.toList());
        // 3. 基于匹配告警重算公共标签、公共注解
        Map<String, String> commonLabels = extractCommonAttributes(matchingAlerts, AlertEvent::getLabels);
        Map<String, String> commonAnnotations = extractCommonAttributes(matchingAlerts, AlertEvent::getAnnotations);
        // 4. 重算分组标签；分组标签变化时重算 groupKey，否则沿用原值
        Map<String, String> groupLabels = extractGroupLabels(alert.getGroupLabels(), commonLabels);
        String groupKey = Objects.equals(groupLabels, alert.getGroupLabels())
                ? alert.getGroupKey() : generateGroupKey(groupLabels);
        // 5. 组装裁剪后的告警组。审计字段（createBy/createTime 等）由 BaseEntity 自动填充，
        //    且此裁剪副本仅用于通知渲染（原告警组已由 store 持久化），无需复制审计字段
        return AlertGroup.builder()
                .id(alert.getId())
                .groupKey(groupKey)
                .status(determineGroupStatus(matchingAlerts))
                .groupLabels(groupLabels)
                .commonLabels(commonLabels)
                .commonAnnotations(commonAnnotations)
                .alertFingerprints(matchingAlerts.stream()
                        .map(AlertEvent::getFingerprint)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()))
                .alerts(matchingAlerts)
                .build();
    }

    /**
     * 提取一批告警中所有告警共有的标签或注解（键值均相同）。
     *
     * @param alerts     告警列表
     * @param attributes 标签或注解提取函数
     * @return 公共属性集合，空列表返回空 map
     */
    private Map<String, String> extractCommonAttributes(
            Collection<AlertEvent> alerts,
            Function<AlertEvent, Map<String, String>> attributes) {
        if (alerts == null || alerts.isEmpty()) {
            return new HashMap<>(0);
        }
        Map<String, String> firstAttributes = attributes.apply(alerts.iterator().next());
        Map<String, String> common =
                firstAttributes == null ? new HashMap<>(0) : new HashMap<>(firstAttributes);
        for (AlertEvent alert : alerts) {
            Map<String, String> current = attributes.apply(alert);
            // 逐步剔除不一致或缺失的键，保留所有告警共有且相同的键值对
            common.keySet().removeIf(key ->
                    current == null || !current.containsKey(key)
                            || !Objects.equals(common.get(key), current.get(key)));
        }
        return common;
    }

    /**
     * 从原始 groupLabels 中保留仍存在于公共标签中的键，作为裁剪后的分组标签。
     *
     * @param originalGroupLabels 原始分组标签
     * @param commonLabels        当前公共标签
     * @return 裁剪后的分组标签
     */
    private Map<String, String> extractGroupLabels(
            Map<String, String> originalGroupLabels,
            Map<String, String> commonLabels) {
        Map<String, String> groupLabels = new HashMap<>(0);
        if (originalGroupLabels != null) {
            originalGroupLabels.keySet().forEach(key -> {
                if (commonLabels.containsKey(key)) {
                    groupLabels.put(key, commonLabels.get(key));
                }
            });
        }
        return groupLabels;
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
                .map(entry -> entry.getKey() + ":" + entry.getValue())
                .collect(Collectors.joining(","));
    }

    /**
     * 根据告警列表决定告警组状态：只要有一条 FIRING 即为 firing，否则为 resolved。
     *
     * @param alerts 告警列表
     * @return 告警组状态码
     */
    private String determineGroupStatus(List<AlertEvent> alerts) {
        return alerts.stream().anyMatch(alert ->
                        AlertStatusEnum.FIRING.getCode().equals(alert.getStatus()))
                ? AlertStatusEnum.FIRING.getCode() : AlertStatusEnum.RESOLVED.getCode();
    }
}
