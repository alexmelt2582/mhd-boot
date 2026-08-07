package com.mhd.alert.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.mhd.boot.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

/**
 * 通知策略表
 */
@TableName(value = "hzb_notice_rule")
@Data
@EqualsAndHashCode(callSuper = true)
public class NoticeRule extends BaseEntity {
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
     * 接收人ID列表(JSON), 如 [4324324, 4324325]
     */
    @TableField(value = "receiver_id", typeHandler = JacksonTypeHandler.class)
    private List<Long> receiverId;

    /**
     * 接收人名称列表(JSON), 如 ["tom","jerry"]
     */
    @TableField(value = "receiver_name", typeHandler = JacksonTypeHandler.class)
    private List<String> receiverName;

    /**
     * 通知模板ID
     */
    @TableField(value = "template_id")
    private Long templateId;

    /**
     * 通知模板名称
     */
    @TableField(value = "template_name")
    private String templateName;

    /**
     * 是否启用: 1-启用 0-禁用
     */
    @TableField(value = "enable")
    private Integer enable;

    /**
     * 是否匹配指定的标签: 1-是 0-否(跳过标签匹配)
     * 1-匹配指定的标签
     * 0-跳过标签匹配
     */
    @TableField(value = "match_specific_label")
    private Integer matchSpecificLabel;

    /**
     * 匹配标签(JSON), match_specific_label=0时有效
     */
    @TableField(value = "labels", typeHandler = JacksonTypeHandler.class)
    private Map<String, String> labels;

    /**
     * 有效星期(JSON), 如 [0,1]
     */
    @TableField(value = "days", typeHandler = JacksonTypeHandler.class)
    private List<Integer> days;

    /**
     * 限制时段开始
     */
    @TableField(value = "period_start")
    private String periodStart;

    /**
     * 限制时段结束
     */
    @TableField(value = "period_end")
    private String periodEnd;
}