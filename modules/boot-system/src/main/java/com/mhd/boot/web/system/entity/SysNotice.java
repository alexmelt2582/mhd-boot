package com.mhd.boot.web.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mhd.boot.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知公告表
 *
 * @author zhao-hao-dong
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_notice")
public class SysNotice extends BaseEntity {
    /**
     * 公告ID
     */
    @TableId(value = "notice_id")
    private Long noticeId;
    /**
     * 公告标题
     */
    @TableField(value = "notice_title")
    private String noticeTitle;
    /**
     * 公告类型（1通知 2公告）（字典获取）
     */
    @TableField(value = "notice_type")
    private String noticeType;
    /**
     * 公告内容
     */
    @TableField(value = "notice_content")
    private String noticeContent;
    /**
     * 公告状态（0正常 1关闭）
     */
    @TableField(value = "status")
    private String status;
    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;
}
