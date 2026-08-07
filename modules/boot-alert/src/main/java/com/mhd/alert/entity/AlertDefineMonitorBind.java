package com.mhd.alert.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mhd.boot.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 告警规则与监控项绑定关系表
 */
@TableName(value = "hzb_alert_define_monitor_bind")
@Data
@EqualsAndHashCode(callSuper = true)
public class AlertDefineMonitorBind extends BaseEntity {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 告警规则ID
     */
    @TableField(value = "alert_define_id")
    private Long alertDefineId;

    /**
     * 监控项ID
     */
    @TableField(value = "monitor_id")
    private Long monitorId;
}