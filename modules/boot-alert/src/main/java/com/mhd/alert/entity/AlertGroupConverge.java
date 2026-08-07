package com.mhd.alert.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mhd.boot.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 告警分组收敛策略表
 */
@TableName(value = "hzb_alert_group_converge")
@Data
@EqualsAndHashCode(callSuper = true)
public class AlertGroupConverge extends BaseEntity {
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
     * 分组标签列表(JSON), 如 ["instance"]
     */
    @TableField(value = "group_labels")
    private String groupLabels;

    /**
     * 首次发送分组告警前的等待时间(秒)
     */
    @TableField(value = "group_wait")
    private Long groupWait;

    /**
     * 分组告警发送间隔(秒)
     */
    @TableField(value = "group_interval")
    private Long groupInterval;

    /**
     * 重复告警间隔(秒), 设为0则不重复
     */
    @TableField(value = "repeat_interval")
    private Long repeatInterval;

    /**
     * 是否启用: 1-启用 0-禁用
     */
    @TableField(value = "enable")
    private Integer enable;
}