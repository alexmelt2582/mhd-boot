package com.mhd.boot.web.system.service;

import com.mhd.boot.common.mybatis.core.domain.PageInfo;
import com.mhd.boot.common.mybatis.core.domain.PageParam;
import com.mhd.boot.common.respnsedata.BaseResponse;
import com.mhd.boot.web.system.model.dto.SysDictItemDTO;
import com.mhd.boot.web.system.model.vo.SysDictItemVo;

import java.util.List;

/**
 * 字典 业务层
 *
 * @author zhao-hao-dong
 */
public interface SysDictItemService {
    /**
     * 分页查询字典数据列表
     *
     * @param dto       查询条件
     * @param pageParam 分页参数
     * @return 字典数据分页列表
     */
    BaseResponse<PageInfo<SysDictItemVo>> selectPageDictItemList(SysDictItemDTO dto, PageParam pageParam);

    /**
     * 根据条件查询字典数据
     *
     * @param dto 字典数据信息
     * @return 字典数据集合信息
     */
    List<SysDictItemVo> selectDictItemList(SysDictItemDTO dto);

    /**
     * 根据字典类型和字典键值查询字典数据信息
     *
     * @param dictType  字典类型
     * @param dictValue 字典键值
     * @return 字典标签
     */
    String selectDictLabel(String dictType, String dictValue);

    /**
     * 根据字典数据ID查询信息
     *
     * @param dictItemId 字典数据ID
     * @return 字典数据
     */
    SysDictItemVo selectDictItemById(Long dictItemId);

    /**
     * 批量删除字典数据信息
     *
     * @param dictItemIds 需要删除的字典数据ID列表
     */
    void deleteDictItemByIds(List<Long> dictItemIds);

    /**
     * 新增保存字典数据信息
     *
     * @param dto 字典数据信息
     * @return 结果
     */
    List<SysDictItemVo> insertDictItem(SysDictItemDTO dto);

    /**
     * 修改保存字典数据信息
     *
     * @param dto 字典数据信息
     * @return 结果
     */
    List<SysDictItemVo> updateDictItem(SysDictItemDTO dto);

    /**
     * 校验字典键值是否唯一
     *
     * @param dto 字典数据
     * @return 结果
     */
    boolean checkDictItemUnique(SysDictItemDTO dto);
}
