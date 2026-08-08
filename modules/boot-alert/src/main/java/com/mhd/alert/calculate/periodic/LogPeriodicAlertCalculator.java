package com.mhd.alert.calculate.periodic;

import com.mhd.alert.constants.AlertConstants;
import com.mhd.alert.entity.AlertRule;
import com.mhd.alert.entity.AlertSingle;
import com.mhd.alert.enums.AlertStatusEnum;
import com.mhd.alert.enums.EnableEnum;
import com.mhd.alert.reduce.AlarmCommonReduce;
import com.mhd.alert.service.DataSourceService;
import com.mhd.alert.utils.AlertTemplateUtils;
import com.mhd.boot.common.utils.StringUtils;
import com.mhd.boot.common.utils.collection.CollectionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 日志类周期性告警计算器。
 *
 * <p>周期性执行告警规则：根据规则表达式查询数据源，对命中阈值的结果按
 * {@code individual}（逐条）或 {@code group}（分组）模式生成告警，
 * 最终交由 {@link AlarmCommonReduce} 进行去重收敛并下发通知。
 *
 * @author zhao-hao-dong
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LogPeriodicAlertCalculator {
    /**
     * 分组告警指纹中用于标识匹配数据条数的标签键
     */
    private static final String ROWS = "__rows__";
    private final DataSourceService dataSourceService;
    private final AlarmCommonReduce alarmCommonReduce;

    /**
     * 周期性告警计算入口。
     *
     * <p>执行流程：
     * <ol>
     *   <li>校验规则启用状态与表达式是否为空，不满足则直接跳过；</li>
     *   <li>委托 {@link #doCalculate(AlertRule)} 执行实际查询与告警生成；</li>
     *   <li>捕获计算过程中的异常并记录错误日志，避免单条规则异常中断整个调度周期。</li>
     * </ol>
     *
     * @param rule 待计算的告警规则
     */
    public void calculate(AlertRule rule) {
        // 1. 前置校验：规则被禁用或表达式为空时直接终止，避免无效查询
        if (Objects.equals(rule.getEnable(), EnableEnum.DISABLE.getCode()) ||
                StringUtils.isBlank(rule.getExpr())) {
            log.error("Alert rule {} is disabled or expression is empty", rule.getName());
            return;
        }
        try {
            // 2. 执行实际的数据查询与告警生成逻辑
            doCalculate(rule);
        } catch (Exception e) {
            // 3. 兜底捕获：单条规则异常不应影响其他规则的调度
            log.error("Calculate periodic rule {} failed: {}", rule.getName(), e.getMessage());
        }
    }

    /**
     * 执行单条规则的实际计算逻辑。
     *
     * <p>执行流程：
     * <ol>
     *   <li>通过数据源服务执行规则表达式查询命中数据；</li>
     *   <li>结果为空则直接返回，表示当前周期无告警；</li>
     *   <li>命中数据交由 {@link #afterThresholdRuleMatch(List, AlertRule)} 处理告警生成。</li>
     * </ol>
     *
     * <p>注意：此处异常被静默吞掉，外层 {@link #calculate(AlertRule)} 已有兜底日志，
     * 此处再抛出会导致重复记录，因此选择忽略。
     *
     * @param rule 待计算的告警规则
     */
    private void doCalculate(AlertRule rule) {
        try {
            // 1. 根据规则绑定的数据源与表达式查询命中阈值的数据
            List<Map<String, Object>> results = dataSourceService.query(rule.getDatasource(), rule.getExpr());
            // 2. 无命中数据则当前周期不产生告警，直接返回
            if (CollectionUtils.isEmpty(results)) {
                return;
            }
            // 3. 命中数据进入阈值匹配后的告警生成流程
            afterThresholdRuleMatch(results, rule);
        } catch (Exception ignored) {
            // 静默处理：外层 calculate 已统一记录异常日志，避免重复输出
        }
    }

    /**
     * 阈值匹配后的告警分发处理。
     *
     * <p>执行流程：
     * <ol>
     *   <li>从规则 labels 中解析告警模式（individual / group），缺省为 group；</li>
     *   <li>统一获取当前时间戳作为告警触发时间；</li>
     *   <li>按模式分发：individual 逐条生成告警，group 聚合生成分组告警；
     *       未知模式仅记录告警。</li>
     * </ol>
     *
     * @param alertContext 命中阈值的数据上下文集合，每条元素对应一条命中记录
     * @param rule         当前告警规则
     */
    private void afterThresholdRuleMatch(List<Map<String, Object>> alertContext, AlertRule rule) {
        // 1. 解析告警模式，决定后续按逐条还是分组方式生成告警
        String alertMode = getAlertMode(rule);
        // 2. 统一获取触发时间戳，保证同一规则同批次告警时间一致
        long currentTime = System.currentTimeMillis();
        switch (alertMode) {
            case AlertConstants.ALERT_MODE_INDIVIDUAL -> {
                // 逐条模式：每条命中数据独立生成一条告警，各自收敛
                for (Map<String, Object> context : alertContext) {
                    generateIndividualAlert(rule, context, currentTime);
                }
            }
            case AlertConstants.ALERT_MODE_GROUP -> {
                // 分组模式：整批命中数据作为一个告警组统一收敛下发
                generateGroupAlert(rule, alertContext, currentTime);
            }
            default -> {
                log.warn("Unknown alert mode for define {}: {}", rule.getName(), alertMode);
            }
        }
    }

    /**
     * 获取告警规则配置的告警模式。
     *
     * <p>从规则 labels 的 {@code __alert_mode__} 键中读取模式值，未配置或为空时
     * 缺省返回 {@link AlertConstants#ALERT_MODE_GROUP}，即默认按分组模式处理。
     *
     * @param rule 当前告警规则
     * @return 告警模式字符串（individual 或 group）
     */
    private String getAlertMode(AlertRule rule) {
        String mode = null;
        // 优先从规则 labels 中读取显式配置的告警模式
        if (rule.getLabels() != null) {
            mode = rule.getLabels().get(AlertConstants.LABEL_ALERT_MODE);
        }
        // 未配置或为空时，缺省采用分组模式，保证规则可用性
        if (mode == null || mode.isEmpty()) {
            return AlertConstants.ALERT_MODE_GROUP;
        } else {
            return mode;
        }
    }

    /**
     * 为单条命中数据生成独立的告警并下发收敛。
     *
     * <p>执行流程：
     * <ol>
     *   <li>构建告警 labels：先填充规则级公共指纹，再将命中数据上下文追加为标签；</li>
     *   <li>构建字段值映射，用于渲染告警模板与注解；</li>
     *   <li>渲染注解与告警内容模板，组装 {@link AlertSingle}（触发次数固定为 1）；</li>
     *   <li>克隆后交由 {@link AlarmCommonReduce} 收敛下发，避免后续修改影响已发送对象。</li>
     * </ol>
     *
     * @param rule        当前告警规则
     * @param context     单条命中数据上下文
     * @param currentTime 告警触发时间戳
     */
    private void generateIndividualAlert(AlertRule rule, Map<String, Object> context, long currentTime) {

        Map<String, String> alertLabels = new HashMap<>(8);

        // 1. 构建告警 labels：先放入规则级公共指纹（告警名、规则 ID、规则 labels）
        Map<String, String> commonFingerPrints = createCommonFingerprints(rule);
        alertLabels.putAll(commonFingerPrints);
        // 将命中数据上下文作为标签追加，便于后续按维度做告警指纹与收敛
        addContextToMap(context, alertLabels);

        // 2. 构建字段值映射（上下文 + 规则 labels），用于模板与注解渲染
        Map<String, Object> fieldValueMap = createFieldValueMap(context, rule);
        // 3. 渲染注解模板与告警内容，并组装告警对象
        Map<String, String> alertAnnotations = createAlertAnnotations(rule, fieldValueMap);
        AlertSingle alert = AlertSingle.builder()
                .labels(alertLabels)
                .annotations(alertAnnotations)
                .content(AlertTemplateUtils.render(rule.getTemplate(), fieldValueMap))
                .status(AlertStatusEnum.FIRING.getCode())
                .triggerTimes(1)
                .startAt(currentTime)
                .activeAt(currentTime)
                .build();

        // 4. 克隆后下发收敛，防止共享引用导致后续聚合逻辑污染已发送对象
        alarmCommonReduce.reduceAndSendAlarm(alert.clone());

        log.debug("Generated individual alert for rule: {}", rule.getName());
    }

    /**
     * 将命中数据上下文转换为字符串后追加到告警 labels 中。
     *
     * <p>跳过 value 为 null 的键，避免 labels 中出现空值导致指纹不稳定。
     *
     * @param context     命中数据上下文
     * @param alertLabels 待填充的告警 labels 集合
     */
    private void addContextToMap(Map<String, Object> context, Map<String, String> alertLabels) {
        for (Map.Entry<String, Object> entry : context.entrySet()) {
            // 仅追加非空值，统一转字符串以兼容 labels 的 String 类型约束
            if (entry.getValue() != null) {
                alertLabels.put(entry.getKey(), entry.getValue().toString());
            }
        }
    }

    /**
     * 为整批命中数据生成分组告警并统一收敛下发。
     *
     * <p>执行流程：
     * <ol>
     *   <li>构建规则级公共指纹，并补充命中条数 {@code __rows__} 与分组模式标识，
     *       作为整组告警的统一收敛键；</li>
     *   <li>遍历每条命中数据，组装各自的 labels / 注解 / 内容，triggerTimes 取整批数量；</li>
     *   <li>克隆每条告警后加入集合，避免共享引用被后续修改；</li>
     *   <li>以公共指纹为收敛键，整组下发至 {@link AlarmCommonReduce} 统一收敛。</li>
     * </ol>
     *
     * @param rule         当前告警规则
     * @param alertContext 命中阈值的数据上下文集合
     * @param currentTime  告警触发时间戳
     */
    private void generateGroupAlert(AlertRule rule, List<Map<String, Object>> alertContext, long currentTime) {

        List<AlertSingle> alerts = new ArrayList<>(alertContext.size());

        // 1. 构建公共指纹：含告警名、规则 ID、规则 labels，作为整组收敛键
        Map<String, String> commonFingerPrints = createCommonFingerprints(rule);

        // 补充分组维度信息：命中条数与模式标识，便于下游收敛与展示
        commonFingerPrints.put(ROWS, String.valueOf(alertContext.size()));
        commonFingerPrints.put(AlertConstants.LABEL_ALERT_MODE, AlertConstants.ALERT_MODE_GROUP);

        // 2. 遍历命中数据，逐条组装告警对象
        for (Map<String, Object> context : alertContext) {

            Map<String, String> alertLabels = new HashMap<>(8);

            alertLabels.putAll(commonFingerPrints);
            addContextToMap(context, alertLabels);

            // 构建字段值映射并渲染注解与内容模板
            Map<String, Object> fieldValueMap = createFieldValueMap(context, rule);
            Map<String, String> alertAnnotations = createAlertAnnotations(rule, fieldValueMap);
            AlertSingle alert = AlertSingle.builder()
                    .labels(alertLabels)
                    .annotations(alertAnnotations)
                    .content(AlertTemplateUtils.render(rule.getTemplate(), fieldValueMap))
                    .status(AlertStatusEnum.FIRING.getCode())
                    // 分组模式下触发次数取整批命中数，体现聚合后的告警强度
                    .triggerTimes(alertContext.size())
                    .startAt(currentTime)
                    .activeAt(currentTime)
                    .build();
            // 3. 克隆后入集合，避免外部引用被后续逻辑篡改
            alerts.add(alert.clone());
        }
        // 4. 以公共指纹为收敛键，整组下发统一收敛
        alarmCommonReduce.reduceAndSendAlarmGroup(commonFingerPrints, alerts);

        log.debug("Generated group alert for rule: {} with {} matching data",
                rule.getName(), alertContext.size());
    }


    /**
     * 创建告警规则的公共指纹集合。
     *
     * <p>指纹包含告警名称、规则 ID 以及规则自身配置的 labels，作为告警收敛与
     * 去重的公共维度。同一规则产生的所有告警共享该指纹的公共部分。
     *
     * @param rule 当前告警规则
     * @return 公共指纹 map
     */
    private Map<String, String> createCommonFingerprints(AlertRule rule) {
        Map<String, String> fingerprints = new HashMap<>(8);
        // 写入告警名与规则 ID，作为最小收敛维度
        fingerprints.put(AlertConstants.LABEL_ALERT_NAME, rule.getName());
        fingerprints.put(AlertConstants.LABEL_DEFINE_ID, String.valueOf(rule.getId()));

        // 合并规则自定义 labels，扩展收敛与展示维度
        if (rule.getLabels() != null) {
            fingerprints.putAll(rule.getLabels());
        }

        return fingerprints;
    }

    /**
     * 创建用于模板渲染的字段值映射。
     *
     * <p>合并命中数据上下文（value 统一转字符串）与规则 labels，
     * 供告警内容模板与注解模板进行变量替换。
     *
     * @param context 命中数据上下文
     * @param rule    当前告警规则
     * @return 字段值映射，value 均为字符串
     */
    private Map<String, Object> createFieldValueMap(Map<String, Object> context, AlertRule rule) {
        Map<String, Object> fieldValueMap = new HashMap<>(8);
        // 将上下文非空值转为字符串，保证模板渲染时类型一致
        for (Map.Entry<String, Object> entry : context.entrySet()) {
            if (entry.getValue() != null) {
                fieldValueMap.put(entry.getKey(), entry.getValue().toString());
            }
        }
        // 合并规则 labels，使模板可引用规则级变量
        if (rule.getLabels() != null) {
            fieldValueMap.putAll(rule.getLabels());
        }

        return fieldValueMap;
    }

    /**
     * 创建告警注解集合。
     *
     * <p>遍历规则配置的注解模板，使用字段值映射渲染每个注解的值，
     * 最终生成可直接展示的注解键值对。
     *
     * @param rule          当前告警规则
     * @param fieldValueMap 用于模板渲染的字段值映射
     * @return 渲染后的注解 map
     */
    private Map<String, String> createAlertAnnotations(AlertRule rule, Map<String, Object> fieldValueMap) {
        Map<String, String> annotations = new HashMap<>(8);

        // 逐条渲染注解模板，将占位符替换为实际字段值
        if (rule.getAnnotations() != null) {
            for (Map.Entry<String, String> entry : rule.getAnnotations().entrySet()) {
                annotations.put(entry.getKey(),
                        AlertTemplateUtils.render(entry.getValue(), fieldValueMap));
            }
        }

        return annotations;
    }
}
