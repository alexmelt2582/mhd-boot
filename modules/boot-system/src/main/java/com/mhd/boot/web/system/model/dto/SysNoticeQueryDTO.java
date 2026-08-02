package com.mhd.boot.web.system.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zhao-hao-dong
 */
@Data
public class SysNoticeQueryDTO implements Serializable {
    /**
     * 公告标题
     */
    private String noticeTitle;
    /**
     * 公告类型（1通知 2公告）字典获取
     */
    private String noticeType;
    /**
     * 公告状态（0正常 1关闭）
     */
    private String status;
}
