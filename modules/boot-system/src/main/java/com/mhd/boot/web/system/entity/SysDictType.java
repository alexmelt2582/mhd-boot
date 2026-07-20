package com.mhd.boot.web.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mhd.boot.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典类型表
 *
 * @author zhao-hao-dong
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_type")
public class SysDictType extends BaseEntity {
    /**
     * 字典主键
     */
    @TableId(value = "id")
    private Long id;
    /**
     * 字典名称
     */
    @TableField(value = "dict_name")
    private String dictName;
    /**
     * 字典类型
     */
    @TableField(value = "dict_type")
    private String dictType;
    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;
}
