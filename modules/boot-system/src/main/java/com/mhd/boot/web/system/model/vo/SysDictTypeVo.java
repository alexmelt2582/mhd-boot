package com.mhd.boot.web.system.model.vo;

import com.mhd.boot.web.system.entity.SysDictType;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 字典类型视图对象
 *
 * @author zhao-hao-dong
 */
@Data
@AutoMapper(target = SysDictType.class)
public class SysDictTypeVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 主键
     */
    private Long dictId;
    /**
     * 字典名称
     */
    private String dictName;
    /**
     * 字典类型
     */
    private String dictType;
    /**
     * 备注
     */
    private String remark;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}