package com.mhd.boot.web.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mhd.boot.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典数据表
 *
 * @author zhao-hao-dong
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_data")
public class SysDictData extends BaseEntity {
    /**
     * 字典编码
     */
    @TableId(value = "id")
    private Long id;
    /**
     * 字典排序
     */
    @TableField(value = "dict_sort")
    private Integer dictSort;
    /**
     * 字典标签
     */
    @TableField(value = "dict_label")
    private String dictLabel;
    /**
     * 字典键值
     */
    @TableField(value = "dict_value")
    private String dictValue;
    /**
     * 字典类型
     */
    @TableField(value = "dict_type")
    private String dictType;
    /**
     * 样式属性（其他样式扩展）
     */
    @TableField(value = "css_class")
    private String cssClass;
    /**
     * 表格字典样式
     */
    @TableField(value = "list_class")
    private String listClass;
    /**
     * 是否默认（Y是 N否）
     */
    @TableField(value = "is_default")
    private String isDefault;
    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;
}
