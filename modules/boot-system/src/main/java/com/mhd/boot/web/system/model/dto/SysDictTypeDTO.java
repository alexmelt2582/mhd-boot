package com.mhd.boot.web.system.model.dto;

import com.mhd.boot.web.system.entity.SysDictType;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 字典类型业务对象
 *
 * @author zhao-hao-dong
 */
@Data
@AutoMapper(target = SysDictType.class, reverseConvertGenerate = false)
public class SysDictTypeDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 字典主键
     */
    private Long id;
    /**
     * 字典名称
     */
    @NotBlank(message = "字典名称不能为空")
    @Size(min = 0, max = 100, message = "字典类型名称长度不能超过{max}个字符")
    private String dictName;
    /**
     * 字典类型
     */
    @NotBlank(message = "字典类型不能为空")
    @Size(min = 0, max = 100, message = "字典类型类型长度不能超过{max}个字符")
    //@Pattern(regexp = RegexConstants.DICTIONARY_TYPE, message = "字典类型必须以字母开头，且只能为（小写字母，数字，下滑线）")
    private String dictType;
    /**
     * 备注
     */
    private String remark;
}