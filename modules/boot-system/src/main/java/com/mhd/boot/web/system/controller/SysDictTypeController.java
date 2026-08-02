package com.mhd.boot.web.system.controller;

import com.baomidou.lock.annotation.Lock4j;
import com.mhd.boot.common.idempotent.annotation.RepeatSubmit;
import com.mhd.boot.common.mybatis.core.domain.PageInfo;
import com.mhd.boot.common.mybatis.core.domain.PageParam;
import com.mhd.boot.common.operatelog.core.annotation.OperateLog;
import com.mhd.boot.common.operatelog.core.enums.OperateTypeEnum;
import com.mhd.boot.common.responsedata.BaseResponse;
import com.mhd.boot.common.responsedata.BaseResultUtils;
import com.mhd.boot.web.system.model.dto.SysDictTypeDTO;
import com.mhd.boot.web.system.model.vo.SysDictTypeVo;
import com.mhd.boot.web.system.service.SysDictTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 数据字典信息
 *
 * @author zhao-hao-dong
 **/
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/dict/type")
public class SysDictTypeController {
    private final SysDictTypeService dictTypeService;

    /**
     * 分页查询字典类型列表
     */
    @GetMapping("/page")
    public BaseResponse<PageInfo<SysDictTypeVo>> page(SysDictTypeDTO dictType, PageParam pageParam) {
        return dictTypeService.selectPageDictTypeList(dictType, pageParam);
    }

    /**
     * 查询字典类型详细
     *
     * @param dictId 字典ID
     */
    @GetMapping(value = "/{dictId}")
    public BaseResponse<SysDictTypeVo> getInfo(@PathVariable Long dictId) {
        return BaseResultUtils.successOfData(dictTypeService.selectDictTypeById(dictId));
    }

    /**
     * 新增字典类型
     */
    @OperateLog(module = "字典类型", type = OperateTypeEnum.CREATE)
    @RepeatSubmit()
    @PostMapping
    public BaseResponse<Void> add(@Validated @RequestBody SysDictTypeDTO dict) {
        if (!dictTypeService.checkDictTypeUnique(dict)) {
            return BaseResultUtils.error("新增字典'" + dict.getDictName() + "'失败，字典类型已存在");
        }
        dictTypeService.insertDictType(dict);
        return BaseResultUtils.success();
    }

    /**
     * 修改字典类型
     */
    @OperateLog(module = "字典类型", type = OperateTypeEnum.UPDATE)
    @RepeatSubmit()
    @PutMapping
    public BaseResponse<Void> edit(@Validated @RequestBody SysDictTypeDTO dict) {
        if (!dictTypeService.checkDictTypeUnique(dict)) {
            return BaseResultUtils.error("修改字典'" + dict.getDictName() + "'失败，字典类型已存在");
        }
        dictTypeService.updateDictType(dict);
        return BaseResultUtils.success();
    }

    /**
     * 删除字典类型
     *
     * @param dictIds 字典ID串
     */
    @OperateLog(module = "字典类型", type = OperateTypeEnum.DELETE)
    @DeleteMapping("/{dictIds}")
    public BaseResponse<Void> remove(@PathVariable Long[] dictIds) {
        dictTypeService.deleteDictTypeByIds(Arrays.asList(dictIds));
        return BaseResultUtils.success();
    }

    /**
     * 刷新字典缓存
     */
    @OperateLog(module = "字典类型", type = OperateTypeEnum.OTHER)
    @Lock4j
    @DeleteMapping("/refreshCache")
    public BaseResponse<Void> refreshCache() {
        dictTypeService.resetDictCache();
        return BaseResultUtils.success();
    }

    /**
     * 获取字典选择框列表
     */
    @GetMapping("/optionselect")
    public BaseResponse<List<SysDictTypeVo>> optionselect() {
        List<SysDictTypeVo> dictTypes = dictTypeService.selectDictTypeAll();
        return BaseResultUtils.successOfData(dictTypes);
    }
}
