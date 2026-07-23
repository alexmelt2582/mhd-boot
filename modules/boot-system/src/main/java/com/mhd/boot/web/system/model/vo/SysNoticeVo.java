package com.mhd.boot.web.system.model.vo;

import com.mhd.boot.web.system.entity.SysNotice;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serializable;

/**
 * 公告视图对象
 *
 * @author zhao-hao-dong
 */
@Data
@AutoMapper(target = SysNotice.class)
public class SysNoticeVo implements Serializable {
}
