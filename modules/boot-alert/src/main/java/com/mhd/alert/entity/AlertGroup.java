package com.mhd.alert.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.mhd.boot.common.mybatis.core.domain.BaseEntity;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;

/**
 * 分组告警记录表
 */
@TableName(value = "hzb_alert_group")
@Data
@EqualsAndHashCode(callSuper = true)
public class AlertGroup extends BaseEntity {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 分组键(唯一)
     */
    @TableField(value = "group_key")
    private String groupKey;

    /**
     * 状态: firing / resolved
     */
    @TableField(value = "status")
    private String status;

    /**
     * 分组标签(JSON)
     */
    @TableField(value = "group_labels", typeHandler = JacksonTypeHandler.class)
    private Map<String, String> groupLabels;

    /**
     * 公共标签(JSON)
     */
    @TableField(value = "common_labels", typeHandler = JacksonTypeHandler.class)
    private Map<String, String> commonLabels;

    /**
     * 公共注解(JSON)
     */
    @TableField(value = "common_annotations", typeHandler = JacksonTypeHandler.class)
    private Map<String, String> commonAnnotations;

    /**
     * 关联告警指纹列表(JSON)
     */
    @TableField(value = "alert_fingerprints", typeHandler = JacksonTypeHandler.class)
    private List<String> alertFingerprints;

    @TableField(exist = false)
    private List<AlertSingle> alerts;
}