package com.mhd.alert.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mhd.boot.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知模板表
 */
@TableName(value = "hzb_notice_template")
@Data
@EqualsAndHashCode(callSuper = true)
public class NoticeTemplate extends BaseEntity {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 模板名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 通知方式: 0-SMS 1-Email 2-Webhook 3-微信公众号 4-企微机器人 5-钉钉机器人 6-飞书机器人 7-Telegram 8-Slack 9-Discord 10-企微应用消息
     */
    @TableField(value = "type")
    private Integer type;

    /**
     * 是否预置模板: 1-预置 0-自定义
     */
    @TableField(value = "preset")
    private Integer preset;

    /**
     * 模板内容, 支持FreeMarker语法
     */
    @TableField(value = "content")
    private String content;
}