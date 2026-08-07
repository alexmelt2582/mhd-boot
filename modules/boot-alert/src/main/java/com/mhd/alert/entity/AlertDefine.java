package com.mhd.alert.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mhd.boot.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 告警规则定义表
 */
@TableName(value = "hzb_alert_define")
@Data
@EqualsAndHashCode(callSuper = true)
public class AlertDefine extends BaseEntity {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 告警规则名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 规则类型: realtime_metric / periodic_metric / realtime_log / periodic_log
     */
    @TableField(value = "type")
    private String type;

    /**
     * 告警阈值表达式, 如 usage>90
     */
    @TableField(value = "expr")
    private String expr;

    /**
     * 执行周期/窗口大小(秒), 用于周期规则或日志实时规则
     */
    @TableField(value = "period")
    private Integer period;

    /**
     * 触发次数阈值, 达到后才真正触发告警
     */
    @TableField(value = "times")
    private Integer times;

    /**
     * 标签(JSON), 如 {"status":"success","env":"prod","priority":"critical"}
     */
    @TableField(value = "labels")
    private String labels;

    /**
     * 注解(JSON), 如 {"summary":"High CPU usage"}
     */
    @TableField(value = "annotations")
    private String annotations;

    /**
     * 告警内容模板, 如 Instance {{ $labels.instance }} CPU usage is {{ $value }}%
     */
    @TableField(value = "template")
    private String template;

    /**
     * 数据源类型, 如 PROMETHEUS
     */
    @TableField(value = "datasource")
    private String datasource;

    /**
     * 是否启用: 1-启用 0-禁用
     */
    @TableField(value = "enable")
    private Integer enable;
}