package com.mhd.alert.model.vo;

import com.mhd.alert.entity.AlertRule;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 通知规则 - 视图对象
 *
 * @author zhao-hao-dong
 */
@Data
@AutoMapper(target = AlertRule.class)
public class AlertRuleVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 主键ID
     */
    private Long id;
    /**
     * 告警规则名称
     */
    private String name;
    /**
     * 规则类型: realtime_metric / periodic_metric / realtime_log / periodic_log
     */
    private String type;
    /**
     * 告警阈值表达式, 如 usage>90
     */
    private String expr;
    /**
     * 执行周期/窗口大小(秒), 用于周期规则或日志实时规则
     */
    private Integer period;
    /**
     * 触发次数阈值, 达到后才真正触发告警
     */
    private Integer times;
    /**
     * 标签(JSON), 如 {"status":"success","env":"prod","priority":"critical"}
     */
    private Map<String, String> labels;
    /**
     * 注解(JSON), 如 {"summary":"High CPU usage"}
     */
    private Map<String, String> annotations;
    /**
     * 告警内容模板, 如 Instance {{ $labels.instance }} CPU usage is {{ $value }}%
     */
    private String template;
    /**
     * 数据源类型, 如 PROMETHEUS
     */
    private String datasource;
    /**
     * 是否启用: 1-启用 0-禁用
     */
    private Integer enable;
    /**
     * 创建人
     */
    private Long createBy;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新人
     */
    private Long updateBy;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}