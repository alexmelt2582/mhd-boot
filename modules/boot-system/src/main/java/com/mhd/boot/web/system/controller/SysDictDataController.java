package com.mhd.boot.web.system.controller;

import cn.hutool.core.util.ObjectUtil;
import com.mhd.boot.common.idempotent.annotation.RepeatSubmit;
import com.mhd.boot.common.mybatis.core.domain.PageParam;
import com.mhd.boot.common.mybatis.core.domain.PageResponse;
import com.mhd.boot.common.operatelog.core.annotation.OperateLog;
import com.mhd.boot.common.operatelog.core.enums.OperateTypeEnum;
import com.mhd.boot.common.respnsedata.BaseResponse;
import com.mhd.boot.common.respnsedata.BaseResultUtils;
import com.mhd.boot.web.system.model.dto.SysDictDataDTO;
import com.mhd.boot.web.system.model.vo.SysDictDataVo;
import com.mhd.boot.web.system.service.SysDictDataService;
import com.mhd.boot.web.system.service.SysDictTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 数据字典信息
 *
 * @author zhao-hao-dong
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/dict/data")
public class SysDictDataController {
    private final SysDictTypeService dictTypeService;
    private final SysDictDataService dictDataService;

    /**
     * 查询字典数据列表
     */
    @GetMapping("/list")
    public PageResponse<SysDictDataVo> list(SysDictDataDTO dictData, PageParam pageParam) {
        return dictDataService.selectPageDictDataList(dictData, pageParam);
    }

    /**
     * 查询字典数据详细
     *
     * @param dictCode 字典code
     */
    @GetMapping(value = "/{dictCode}")
    public BaseResponse<SysDictDataVo> getInfo(@PathVariable Long dictCode) {
        return BaseResultUtils.successOfData(dictDataService.selectDictDataById(dictCode));
    }

    /**
     * 根据字典类型查询字典数据信息
     *
     * @param dictType 字典类型
     */
    @GetMapping(value = "/type/{dictType}")
    public BaseResponse<List<SysDictDataVo>> dictType(@PathVariable String dictType) {
        List<SysDictDataVo> data = dictTypeService.selectDictDataByType(dictType);
        if (ObjectUtil.isNull(data)) {
            data = new ArrayList<>();
        }
        return BaseResultUtils.successOfData(data);
    }

    /**
     * 新增字典类型
     */
    @OperateLog(module = "字典数据", type = OperateTypeEnum.CREATE)
    @RepeatSubmit()
    @PostMapping
    public BaseResponse<Void> add(@Validated @RequestBody SysDictDataDTO dict) {
        if (!dictDataService.checkDictDataUnique(dict)) {
            return BaseResultUtils.error("新增字典数据'" + dict.getDictValue() + "'失败，字典键值已存在");
        }
        dictDataService.insertDictData(dict);
        return BaseResultUtils.success();
    }

    /**
     * 修改保存字典类型
     */
    @OperateLog(module = "字典数据", type = OperateTypeEnum.UPDATE)
    @RepeatSubmit()
    @PutMapping
    public BaseResponse<Void> edit(@Validated @RequestBody SysDictDataDTO dict) {
        if (!dictDataService.checkDictDataUnique(dict)) {
            return BaseResultUtils.error("修改字典数据'" + dict.getDictValue() + "'失败，字典键值已存在");
        }
        dictDataService.updateDictData(dict);
        return BaseResultUtils.success();
    }

    /**
     * 删除字典类型
     *
     * @param dictCodes 字典code串
     */
    @OperateLog(module = "字典数据", type = OperateTypeEnum.DELETE)
    @DeleteMapping("/{dictCodes}")
    public BaseResponse<Void> remove(@PathVariable Long[] dictCodes) {
        dictDataService.deleteDictDataByIds(Arrays.asList(dictCodes));
        return BaseResultUtils.success();
    }
}
