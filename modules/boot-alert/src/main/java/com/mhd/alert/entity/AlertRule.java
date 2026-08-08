package com.mhd.alert.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.mhd.boot.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * <p>
 * 告警规则定义表
 * </p>
 *
 * @author zhao-hao-dong
 * @since 2026-08-07
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hzb_alert_rule")
public class AlertRule extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 告警规则名称
     */
    @TableField("name")
    private String name;

    /**
     * 规则类型: realtime_metric / periodic_metric / realtime_log / periodic_log
     */
    @TableField("type")
    private String type;

    /**
     * 告警阈值表达式, 如 usage>90
     */
    @TableField("expr")
    private String expr;

    /**
     * 执行周期/窗口大小(秒), 用于周期规则或日志实时规则
     */
    @TableField("period")
    private Integer period;

    /**
     * 触发次数阈值, 达到后才真正触发告警
     */
    @TableField("times")
    private Integer times;

    /**
     * 标签(JSON), 如 {"status":"success","env":"prod","priority":"critical"}
     */
    @TableField(value = "labels", typeHandler = JacksonTypeHandler.class)
    private Map<String, String> labels;

    /**
     * 注解(JSON), 如 {"summary":"High CPU usage"}
     */
    @TableField(value = "annotations", typeHandler = JacksonTypeHandler.class)
    private Map<String, String> annotations;

    /**
     * 告警内容模板, 如 Instance {{ $labels.instance }} CPU usage is {{ $value }}%
     */
    @TableField("template")
    private String template;

    /**
     * 数据源类型, 如 PROMETHEUS
     */
    @TableField("datasource")
    private String datasource;

    /**
     * 是否启用: 1-启用 0-禁用
     */
    @TableField("enable")
    private Integer enable;
}
