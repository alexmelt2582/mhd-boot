package com.mhd.alert.calculate.realtime;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 实时指标告警计算器（占位实现，待迁移）。
 *
 * <p>原 hertzbeat 的 {@code RealTimeAlertCalculator} 依赖大量外部基础设施：
 * <ul>
 *   <li>{@code CollectRep.MetricsData}：hertzbeat 采集层 protobuf 消息，mhd-boot 暂无对应实现；</li>
 *   <li>{@code CommonDataQueue}：hertzbeat 进程内指标队列，mhd-boot 暂无对应实现；</li>
 *   <li>{@code JexlExprCalculator}：JEXL 表达式引擎，尚未引入；</li>
 *   <li>{@code AlertDefine} / {@code SingleAlert}：已重命名为 {@link com.mhd.alert.entity.AlertRule} /
 *       {@link com.mhd.alert.entity.AlertEvent}，但本计算器的完整迁移需先补齐上述基础设施。</li>
 * </ul>
 *
 * <p>当前收敛→分发→通知主链路（{@code AlarmCommonReduce → AlarmGroupReduce →
 * AlarmInhibitReduce → AlarmSilenceReduce → AlertNoticeDispatch}）已打通，
 * 周期性告警计算（{@link com.mhd.alert.calculate.periodic.LogPeriodicAlertCalculator}、
 * {@link com.mhd.alert.calculate.periodic.MetricsPeriodicAlertCalculator}）已可用，
 * 实时告警计算作为后续阶段单独迁移，避免阻塞当前主链路验证。
 *
 * <p><b>TODO:</b> 引入指标数据队列与 JEXL 表达式引擎后，参照 hertzbeat
 * {@code RealTimeAlertCalculator} 重新实现，并将 {@code AlertDefine}→{@code AlertRule}、
 * {@code SingleAlert}→{@code AlertEvent} 的命名映射补齐。
 *
 * @author zhao-hao-dong
 */
@Component
@Slf4j
public class MetricsRealTimeAlertCalculator {

    /**
     * 实时计算入口待迁移：原实现从 {@code CommonDataQueue} 拉取 {@code CollectRep.MetricsData}
     * 并逐条匹配实时告警规则。在指标队列与表达式引擎就绪前暂不启用。
     */
    public void startCalculate() {
        log.warn("[Alert] MetricsRealTimeAlertCalculator is pending migration, real-time metric alert is disabled.");
    }
}
