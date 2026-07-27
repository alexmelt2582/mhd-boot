package com.mhd.boot.web.system.controller;

import cn.hutool.core.util.ObjectUtil;
import com.mhd.boot.common.idempotent.annotation.RepeatSubmit;
import com.mhd.boot.common.mybatis.core.domain.PageInfo;
import com.mhd.boot.common.mybatis.core.domain.PageParam;
import com.mhd.boot.common.operatelog.core.annotation.OperateLog;
import com.mhd.boot.common.operatelog.core.enums.OperateTypeEnum;
import com.mhd.boot.common.respnsedata.BaseResponse;
import com.mhd.boot.common.respnsedata.BaseResultUtils;
import com.mhd.boot.web.system.model.dto.SysDictItemDTO;
import com.mhd.boot.web.system.model.vo.SysDictItemVo;
import com.mhd.boot.web.system.service.SysDictItemService;
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
@RequestMapping("/system/dict/item")
public class SysDictItemController {
    private final SysDictTypeService dictTypeService;
    private final SysDictItemService dictDataService;

    /**
     * 分页查询字典数据列表
     */
    @GetMapping("/page")
    public BaseResponse<PageInfo<SysDictItemVo>> page(SysDictItemDTO sysDictItemDTO, PageParam pageParam) {
        return dictDataService.selectPageDictItemList(sysDictItemDTO, pageParam);
    }

    /**
     * 查询字典数据详细
     *
     * @param dictItemId 字典数据ID
     */
    @GetMapping(value = "/{dictItemId}")
    public BaseResponse<SysDictItemVo> getInfo(@PathVariable Long dictItemId) {
        return BaseResultUtils.successOfData(dictDataService.selectDictItemById(dictItemId));
    }

    /**
     * 根据字典类型查询字典数据信息
     *
     * @param dictType 字典类型
     */
    @GetMapping(value = "/type/{dictType}")
    public BaseResponse<List<SysDictItemVo>> dictType(@PathVariable String dictType) {
        List<SysDictItemVo> data = dictTypeService.selectDictDataByType(dictType);
        if (ObjectUtil.isNull(data)) {
            data = new ArrayList<>();
        }
        return BaseResultUtils.successOfData(data);
    }

    /**
     * 新增字典数据
     */
    @OperateLog(module = "字典数据", type = OperateTypeEnum.CREATE)
    @RepeatSubmit()
    @PostMapping
    public BaseResponse<Void> add(@Validated @RequestBody SysDictItemDTO dto) {
        if (!dictDataService.checkDictItemUnique(dto)) {
            return BaseResultUtils.error("新增字典数据'" + dto.getDictValue() + "'失败，字典键值已存在");
        }
        dictDataService.insertDictItem(dto);
        return BaseResultUtils.success();
    }

    /**
     * 修改保存字典数据
     */
    @OperateLog(module = "字典数据", type = OperateTypeEnum.UPDATE)
    @RepeatSubmit()
    @PutMapping
    public BaseResponse<Void> edit(@Validated @RequestBody SysDictItemDTO dto) {
        if (!dictDataService.checkDictItemUnique(dto)) {
            return BaseResultUtils.error("修改字典数据'" + dto.getDictValue() + "'失败，字典键值已存在");
        }
        dictDataService.updateDictItem(dto);
        return BaseResultUtils.success();
    }

    /**
     * 删除字典数据
     *
     * @param dictItemIds 字典数据ID数组
     */
    @OperateLog(module = "字典数据", type = OperateTypeEnum.DELETE)
    @DeleteMapping("/{dictItemIds}")
    public BaseResponse<Void> remove(@PathVariable Long[] dictItemIds) {
        dictDataService.deleteDictItemByIds(Arrays.asList(dictItemIds));
        return BaseResultUtils.success();
    }
}
