package com.mhd.alert.calculate.periodic;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mhd.alert.entity.AlertRule;
import com.mhd.alert.enums.AlertRuleTypeEnum;
import com.mhd.alert.service.AlertRuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author zhao-hao-dong
 */
@Slf4j
@Component
public class PeriodicAlertRuleScheduler implements CommandLineRunner, DisposableBean {
    private final AlertRuleService alertRuleService;
    private final LogPeriodicAlertCalculator logPeriodicAlertCalculator;
    private final MetricsPeriodicAlertCalculator metricsPeriodicAlertCalculator;
    private final ScheduledExecutorService scheduledExecutor;
    private final ConcurrentHashMap<Long, ScheduledTaskState> scheduledTasks = new ConcurrentHashMap<>();

    public PeriodicAlertRuleScheduler(AlertRuleService alertRuleService,
                                      LogPeriodicAlertCalculator logPeriodicAlertCalculator,
                                      MetricsPeriodicAlertCalculator metricsPeriodicAlertCalculator
    ) {
        this.alertRuleService = alertRuleService;
        this.logPeriodicAlertCalculator = logPeriodicAlertCalculator;
        this.metricsPeriodicAlertCalculator = metricsPeriodicAlertCalculator;
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setUncaughtExceptionHandler((thread, throwable) -> {
                    log.error("[Alert] Scheduled periodic alert threshold has uncaughtException.");
                    log.error(throwable.getMessage(), throwable);
                })
                .setDaemon(true)
                .setNameFormat("periodic-alert-threshold-worker-%d")
                .build();
        this.scheduledExecutor = Executors.newScheduledThreadPool(10, threadFactory);
    }

    public void cancelSchedule(Long ruleId) {
        if (ruleId == null) {
            return;
        }
        ScheduledTaskState state = scheduledTasks.remove(ruleId);
        if (state != null) {
            state.cancel();
        }
    }

    public void updateSchedule(AlertRule rule) {
        if (rule == null || rule.getId() == null) {
            log.error("[Alert] Rule is null or rule id is null.");
            return;
        }
        cancelSchedule(rule.getId());
        if (AlertRuleTypeEnum.isPeriodicType(rule.getType())) {
            ScheduledTaskState state = new ScheduledTaskState(rule);
            ScheduledFuture<?> future = scheduledExecutor.scheduleAtFixedRate(() -> executeRule(rule),
                    0, rule.getPeriod(), TimeUnit.SECONDS);
            state.setScheduledFuture(future);
            scheduledTasks.put(rule.getId(), state);
            log.info("[Alert] Scheduled periodic alert rule {} with period {} seconds.", rule.getName(), rule.getPeriod());
        }
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("[Alert] Starting periodic alert rule scheduler...");
        // 1. 加载所有周期性告警规则
        List<AlertRule> metricsPeriodicRules = alertRuleService.selectListByTypeAndEnableTrue(AlertRuleTypeEnum.PERIODIC_METRIC.getCode());
        List<AlertRule> logPeriodicRules = alertRuleService.selectListByTypeAndEnableTrue(AlertRuleTypeEnum.PERIODIC_LOG.getCode());
        List<AlertRule> periodicRules = new ArrayList<>(metricsPeriodicRules.size() + logPeriodicRules.size());
        periodicRules.addAll(metricsPeriodicRules);
        periodicRules.addAll(logPeriodicRules);

        // 2. 初始化所有周期性告警规则的定时任务
        for (AlertRule rule : periodicRules) {
            updateSchedule(rule);
        }
    }

    @Override
    public void destroy() throws Exception {
        scheduledTasks.values().forEach(ScheduledTaskState::cancel);
        scheduledTasks.clear();
        scheduledExecutor.shutdownNow();
    }

    private final class ScheduledTaskState {
        private final AlertRule rule;
        private ScheduledFuture<?> scheduledFuture;
        private Future<?> runningFuture;
        private boolean running;
        private boolean pending;
        private boolean cancelled;

        private ScheduledTaskState(AlertRule rule) {
            this.rule = rule;
        }

        private synchronized void setScheduledFuture(ScheduledFuture<?> scheduledFuture) {
            this.scheduledFuture = scheduledFuture;
        }

        private synchronized void cancel() {
            cancelled = true;
            pending = false;
            ScheduledFuture<?> periodicFuture = scheduledFuture;
            Future<?> currentFuture = runningFuture;
            if (periodicFuture != null) {
                periodicFuture.cancel(true);
            }
            if (currentFuture != null) {
                currentFuture.cancel(true);
            }
        }
    }

    private void executeRule(AlertRule rule) {
        if (AlertRuleTypeEnum.PERIODIC_LOG.getCode().equalsIgnoreCase(rule.getType())) {
            logPeriodicAlertCalculator.calculate(rule);
        } else if (AlertRuleTypeEnum.PERIODIC_METRIC.getCode().equalsIgnoreCase(rule.getType())) {
            metricsPeriodicAlertCalculator.calculate(rule);
        }
    }
}
