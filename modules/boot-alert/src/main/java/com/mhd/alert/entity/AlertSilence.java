package com.mhd.alert.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mhd.boot.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 告警静默策略表
 */
@TableName(value = "hzb_alert_silence")
@Data
@EqualsAndHashCode(callSuper = true)
public class AlertSilence extends BaseEntity {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 策略名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 是否启用: 1-启用 0-禁用
     */
    @TableField(value = "enable")
    private Integer enable;

    /**
     * 是否匹配所有告警: 1-是 0-否
     */
    @TableField(value = "match_all")
    private Integer matchAll;

    /**
     * 静默类型: 0-一次性 1-周期性
     */
    @TableField(value = "type")
    private Integer type;

    /**
     * 已静默告警次数
     */
    @TableField(value = "times")
    private Integer times;

    /**
     * 匹配标签(JSON), 如 {"key1":"value1"}
     */
    @TableField(value = "labels")
    private String labels;

    /**
     * 周期静默有效星期(JSON), 如 [0,1]; 7=周日 1=周一 ... 6=周六
     */
    @TableField(value = "days")
    private String days;

    /**
     * 限制时段开始, 如 00:00:00
     */
    @TableField(value = "period_start")
    private String periodStart;

    /**
     * 限制时段结束, 如 23:59:59
     */
    @TableField(value = "period_end")
    private String periodEnd;
}