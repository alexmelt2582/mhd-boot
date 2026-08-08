package com.mhd.alert.calculate.periodic;

import com.mhd.alert.constants.AlertConstants;
import com.mhd.alert.entity.AlertEvent;
import com.mhd.alert.entity.AlertRule;
import com.mhd.alert.enums.AlertStatusEnum;
import com.mhd.alert.enums.EnableEnum;
import com.mhd.alert.reduce.AlarmCommonReduce;
import com.mhd.alert.service.DataSourceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link LogPeriodicAlertCalculator} 单元测试。
 *
 * <p>覆盖：规则启用/禁用与表达式校验、空结果短路、individual / group 两种告警模式、
 * 缺省模式、未知模式，以及生成告警的 labels / content / triggerTimes / status 断言。
 *
 * @author zhao-hao-dong
 */
@ExtendWith(MockitoExtension.class)
class LogPeriodicAlertCalculatorTest {

    private static final String ROWS = "__rows__";

    @Mock
    private DataSourceService dataSourceService;

    @Mock
    private AlarmCommonReduce alarmCommonReduce;

    @InjectMocks
    private LogPeriodicAlertCalculator calculator;

    private AlertRule groupAlertRule;
    private AlertRule individualAlertRule;

    @BeforeEach
    void setUp() {
        groupAlertRule = buildRule(AlertConstants.ALERT_MODE_GROUP);
        individualAlertRule = buildRule(AlertConstants.ALERT_MODE_INDIVIDUAL);
    }

    /**
     * 构建一个启用的日志告警规则，仅告警模式不同。
     *
     * @param mode 告警模式（group / individual）
     * @return 已填充字段的 AlertRule
     */
    private AlertRule buildRule(String mode) {
        AlertRule rule = new AlertRule();
        rule.setId(1L);
        rule.setName("upload_log_error_alert");
        rule.setType("periodic_log");
        rule.setExpr("SELECT * FROM upload_logs WHERE severity_text = 'ERROR'");
        Map<String, String> labels = new HashMap<>(4);
        labels.put("team", "backend");
        labels.put(AlertConstants.LABEL_ALERT_MODE, mode);
        rule.setLabels(labels);
        Map<String, String> annotations = new HashMap<>(4);
        annotations.put("summary", "Error logs detected");
        annotations.put("description", "Multiple error logs found");
        rule.setAnnotations(annotations);
        rule.setTemplate("Found ${severity_text} log: ${body} from ${service_name}");
        rule.setDatasource("sql");
        rule.setEnable(EnableEnum.ENABLE.getCode());
        rule.setPeriod(300);
        return rule;
    }

    /**
     * 构造模拟的日志查询命中结果。
     *
     * @param count 命中条数
     * @return 命中数据集合
     */
    private List<Map<String, Object>> createSampleLogResults(int count) {
        List<Map<String, Object>> results = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            Map<String, Object> row = new HashMap<>(8);
            row.put("severity_text", "ERROR");
            row.put("body", "NullPointerException at line " + i);
            row.put("service_name", "order-service");
            results.add(row);
        }
        return results;
    }

    @Test
    @DisplayName("启用规则 + group 模式：查询数据源并整组下发告警")
    void testCalculateWithEnabledRuleAndGroupMode() {
        // Given
        List<Map<String, Object>> queryResults = createSampleLogResults(3);
        when(dataSourceService.query(anyString(), anyString())).thenReturn(queryResults);

        // When
        calculator.calculate(groupAlertRule);

        // Then
        verify(dataSourceService).query("sql", "SELECT * FROM upload_logs WHERE severity_text = 'ERROR'");
        verify(alarmCommonReduce).reduceAndSendAlarmGroup(any(), anyList());
        verify(alarmCommonReduce, never()).reduceAndSendAlarm(any());
    }

    @Test
    @DisplayName("启用规则 + individual 模式：逐条下发告警，次数等于命中条数")
    void testCalculateWithIndividualMode() {
        // Given
        List<Map<String, Object>> queryResults = createSampleLogResults(3);
        when(dataSourceService.query(anyString(), anyString())).thenReturn(queryResults);

        // When
        calculator.calculate(individualAlertRule);

        // Then
        verify(alarmCommonReduce, times(3)).reduceAndSendAlarm(any(AlertEvent.class));
        verify(alarmCommonReduce, never()).reduceAndSendAlarmGroup(any(), anyList());
    }

    @Test
    @DisplayName("禁用规则：不查询数据源、不下发告警")
    void testCalculateWithDisabledRule() {
        // Given
        groupAlertRule.setEnable(EnableEnum.DISABLE.getCode());

        // When
        calculator.calculate(groupAlertRule);

        // Then
        verify(dataSourceService, never()).query(anyString(), anyString());
        verify(alarmCommonReduce, never()).reduceAndSendAlarm(any());
        verify(alarmCommonReduce, never()).reduceAndSendAlarmGroup(any(), anyList());
    }

    @Test
    @DisplayName("表达式为空：不查询数据源、不下发告警")
    void testCalculateWithBlankExpression() {
        // Given
        groupAlertRule.setExpr("   ");

        // When
        calculator.calculate(groupAlertRule);

        // Then
        verify(dataSourceService, never()).query(anyString(), anyString());
        verify(alarmCommonReduce, never()).reduceAndSendAlarmGroup(any(), anyList());
    }

    @Test
    @DisplayName("查询结果为空：当前周期不产生告警")
    void testCalculateWithEmptyQueryResults() {
        // Given
        when(dataSourceService.query(anyString(), anyString())).thenReturn(new ArrayList<>());

        // When
        calculator.calculate(groupAlertRule);

        // Then
        verify(dataSourceService).query(anyString(), anyString());
        verify(alarmCommonReduce, never()).reduceAndSendAlarmGroup(any(), anyList());
    }

    @Test
    @DisplayName("查询结果为 null：不产生告警且不抛异常")
    void testCalculateWithNullQueryResults() {
        // Given
        when(dataSourceService.query(anyString(), anyString())).thenReturn(null);

        // When
        calculator.calculate(groupAlertRule);

        // Then
        verify(alarmCommonReduce, never()).reduceAndSendAlarmGroup(any(), anyList());
    }

    @Test
    @DisplayName("未配置告警模式时缺省按 group 处理")
    void testDefaultModeIsGroupWhenLabelAbsent() {
        // Given
        groupAlertRule.getLabels().remove(AlertConstants.LABEL_ALERT_MODE);
        List<Map<String, Object>> queryResults = createSampleLogResults(2);
        when(dataSourceService.query(anyString(), anyString())).thenReturn(queryResults);

        // When
        calculator.calculate(groupAlertRule);

        // Then
        verify(alarmCommonReduce).reduceAndSendAlarmGroup(any(), anyList());
        verify(alarmCommonReduce, never()).reduceAndSendAlarm(any());
    }

    @Test
    @DisplayName("未知告警模式：不产生告警")
    void testCalculateWithUnknownAlertMode() {
        // Given
        groupAlertRule.getLabels().put(AlertConstants.LABEL_ALERT_MODE, "unknown_mode");
        List<Map<String, Object>> queryResults = createSampleLogResults(2);
        when(dataSourceService.query(anyString(), anyString())).thenReturn(queryResults);

        // When
        calculator.calculate(groupAlertRule);

        // Then
        verify(alarmCommonReduce, never()).reduceAndSendAlarm(any());
        verify(alarmCommonReduce, never()).reduceAndSendAlarmGroup(any(), anyList());
    }

    @Test
    @DisplayName("group 模式：校验告警内容、标签、触发次数与状态")
    void testGroupAlertVerifiesContentAndLabels() {
        // Given
        List<Map<String, Object>> queryResults = createSampleLogResults(2);
        when(dataSourceService.query(anyString(), anyString())).thenReturn(queryResults);

        // When
        calculator.calculate(groupAlertRule);

        // Then
        ArgumentCaptor<Map<String, String>> labelsCaptor = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<List<AlertEvent>> alertsCaptor = ArgumentCaptor.forClass(List.class);
        verify(alarmCommonReduce).reduceAndSendAlarmGroup(labelsCaptor.capture(), alertsCaptor.capture());

        // 校验公共指纹（收敛键）
        Map<String, String> groupLabels = labelsCaptor.getValue();
        assertEquals("upload_log_error_alert", groupLabels.get(AlertConstants.LABEL_ALERT_NAME));
        assertEquals("1", groupLabels.get(AlertConstants.LABEL_RULE_ID));
        assertEquals("2", groupLabels.get(ROWS));
        assertEquals(AlertConstants.ALERT_MODE_GROUP, groupLabels.get(AlertConstants.LABEL_ALERT_MODE));

        // 校验告警集合
        List<AlertEvent> alerts = alertsCaptor.getValue();
        assertEquals(2, alerts.size());

        AlertEvent first = alerts.get(0);
        // 分组模式下触发次数等于整批命中数
        assertEquals(2, first.getTriggerTimes());
        assertEquals(AlertStatusEnum.FIRING.getCode(), first.getStatus());
        assertNotNull(first.getStartAt());
        assertEquals(first.getStartAt(), first.getActiveAt());
        // 校验模板渲染结果
        assertEquals("Found ERROR log: NullPointerException at line 1 from order-service", first.getContent());
        // 校验命中数据上下文已写入 labels
        assertEquals("ERROR", first.getLabels().get("severity_text"));
        assertEquals("order-service", first.getLabels().get("service_name"));
    }

    @Test
    @DisplayName("individual 模式：校验单条告警内容与触发次数为 1")
    void testIndividualAlertVerifiesContent() {
        // Given
        List<Map<String, Object>> queryResults = createSampleLogResults(1);
        when(dataSourceService.query(anyString(), anyString())).thenReturn(queryResults);

        // When
        calculator.calculate(individualAlertRule);

        // Then
        ArgumentCaptor<AlertEvent> alertCaptor = ArgumentCaptor.forClass(AlertEvent.class);
        verify(alarmCommonReduce).reduceAndSendAlarm(alertCaptor.capture());

        AlertEvent alert = alertCaptor.getValue();
        // individual 模式触发次数固定为 1
        assertEquals(1, alert.getTriggerTimes());
        assertEquals(AlertStatusEnum.FIRING.getCode(), alert.getStatus());
        assertEquals("Found ERROR log: NullPointerException at line 1 from order-service", alert.getContent());
        // 校验注解模板已被渲染（仍为原值，因模板无占位符）
        assertEquals("Error logs detected", alert.getAnnotations().get("summary"));
    }

    @Test
    @DisplayName("查询抛异常：被外层兜底捕获，不向下发器传播")
    void testCalculateWhenQueryThrowsException() {
        // Given
        when(dataSourceService.query(anyString(), anyString()))
                .thenThrow(new RuntimeException("datasource unavailable"));

        // When - 不应抛出异常
        calculator.calculate(groupAlertRule);

        // Then
        verify(alarmCommonReduce, never()).reduceAndSendAlarmGroup(any(), anyList());
    }

    @Test
    @DisplayName("命中数据包含 null 值：null 键被跳过，不写入 labels")
    void testCalculateSkipsNullContextValues() {
        // Given
        List<Map<String, Object>> queryResults = createSampleLogResults(1);
        queryResults.get(0).put("empty_field", null);
        when(dataSourceService.query(anyString(), anyString())).thenReturn(queryResults);

        // When
        calculator.calculate(groupAlertRule);

        // Then
        ArgumentCaptor<List<AlertEvent>> alertsCaptor = ArgumentCaptor.forClass(List.class);
        verify(alarmCommonReduce).reduceAndSendAlarmGroup(any(), alertsCaptor.capture());

        AlertEvent alert = alertsCaptor.getValue().get(0);
        assertTrue(alert.getLabels().containsKey("severity_text"));
        // null 值的键不应出现在 labels 中
        assertTrue(!alert.getLabels().containsKey("empty_field"));
    }
}
