package com.mhd.alert.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mhd.boot.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 告警抑制规则表
 */
@TableName(value = "hzb_alert_inhibit")
@Data
@EqualsAndHashCode(callSuper = true)
public class AlertInhibit extends BaseEntity {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 抑制规则名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 源告警匹配标签(JSON), 如 {"severity":"critical","instance":"web-01"}
     */
    @TableField(value = "source_labels")
    private String sourceLabels;

    /**
     * 目标告警匹配标签(JSON), 如 {"severity":"warning","instance":"web-01"}
     */
    @TableField(value = "target_labels")
    private String targetLabels;

    /**
     * 等同标签列表(JSON), 如 ["instance","job"]
     */
    @TableField(value = "equal_labels")
    private String equalLabels;

    /**
     * 是否启用: 1-启用 0-禁用
     */
    @TableField(value = "enable")
    private Integer enable;
}