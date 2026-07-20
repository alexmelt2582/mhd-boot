package com.mhd.boot.web.system.model.dto;

import com.mhd.boot.web.system.entity.SysDictData;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 字典数据视图对象
 *
 * @author zhao-hao-dong
 */
@Data
@AutoMapper(target = SysDictData.class)
public class SysDictDataDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 字典编码
     */
    private Long dictCode;
    /**
     * 字典排序
     */
    private Integer dictSort;
    /**
     * 字典标签
     */
    private String dictLabel;
    /**
     * 字典键值
     */
    private String dictValue;
    /**
     * 字典类型
     */
    private String dictType;
    /**
     * 样式属性（其他样式扩展）
     */
    private String cssClass;
    /**
     * 表格回显样式
     */
    private String listClass;
    /**
     * 是否默认（Y是 N否）
     */
    private String isDefault;
    /**
     * 备注
     */
    private String remark;
    /**
     * 创建时间
     */
    private Date createTime;
}
