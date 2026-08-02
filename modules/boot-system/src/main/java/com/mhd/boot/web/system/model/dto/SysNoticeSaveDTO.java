package com.mhd.boot.web.system.model.dto;

import com.mhd.boot.common.validate.EditGroup;
import com.mhd.boot.web.system.entity.SysNotice;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zhao-hao-dong
 */
@Data
@AutoMapper(target = SysNotice.class, reverseConvertGenerate = false)
public class SysNoticeSaveDTO implements Serializable {
    /**
     * 公告ID
     */
    @NotBlank(message = "公告ID不能为空", groups = {EditGroup.class})
    private Long noticeId;

    /**
     * 公告标题
     */
    @NotBlank(message = "公告标题不能为空")
    @Size(min = 0, max = 50, message = "公告标题不能超过{max}个字符")
    private String noticeTitle;
    /**
     * 公告类型（1通知 2公告）字典获取
     */
    private String noticeType;
    /**
     * 公告内容
     */
    private String noticeContent;
    /**
     * 公告状态（0正常 1关闭）
     */
    private String status;
    /**
     * 备注
     */
    private String remark;
    /**
     * 创建人名称
     */
    private String createByName;
}
