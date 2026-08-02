package com.mhd.boot.web.system.service;

import com.mhd.boot.common.mybatis.core.domain.PageInfo;
import com.mhd.boot.common.mybatis.core.domain.PageParam;
import com.mhd.boot.common.responsedata.BaseResponse;
import com.mhd.boot.web.system.model.dto.SysDictTypeDTO;
import com.mhd.boot.web.system.model.vo.SysDictItemVo;
import com.mhd.boot.web.system.model.vo.SysDictTypeVo;

import java.util.List;

/**
 * 字典 业务层
 *
 * @author zhao-hao-dong
 */
public interface SysDictTypeService {
    /**
     * 分页查询字典类型列表
     *
     * @param sysDictTypeDTO 查询条件
     * @param pageParam      分页参数
     * @return 字典类型分页列表
     */
    BaseResponse<PageInfo<SysDictTypeVo>> selectPageDictTypeList(SysDictTypeDTO sysDictTypeDTO, PageParam pageParam);

    /**
     * 根据条件查询字典类型
     *
     * @param sysDictTypeDTO 查询条件
     * @return 字典类型列表
     */
    List<SysDictTypeVo> selectPageDictTypeList(SysDictTypeDTO sysDictTypeDTO);

    /**
     * 根据所有字典类型
     *
     * @return 字典类型集合信息
     */
    List<SysDictTypeVo> selectDictTypeAll();

    /**
     * 根据字典类型查询字典数据
     *
     * @param dictType 字典类型
     * @return 字典数据集合信息
     */
    List<SysDictItemVo> selectDictDataByType(String dictType);

    /**
     * 根据字典类型ID查询信息
     *
     * @param dictId 字典类型ID
     * @return 字典类型
     */
    SysDictTypeVo selectDictTypeById(Long dictId);

    /**
     * 根据字典类型查询信息
     *
     * @param dictType 字典类型
     * @return 字典类型
     */
    SysDictTypeVo selectDictTypeByType(String dictType);

    /**
     * 批量删除字典信息
     *
     * @param dictIds 需要删除的字典ID
     */
    void deleteDictTypeByIds(List<Long> dictIds);

    /**
     * 重置字典缓存数据
     */
    void resetDictCache();

    /**
     * 新增保存字典类型信息
     *
     * @param sysDictTypeDTO 字典类型信息
     * @return 结果
     */
    List<SysDictTypeVo> insertDictType(SysDictTypeDTO sysDictTypeDTO);

    /**
     * 修改保存字典类型信息
     *
     * @param sysDictTypeDTO 字典类型信息
     * @return 结果
     */
    List<SysDictItemVo> updateDictType(SysDictTypeDTO sysDictTypeDTO);

    /**
     * 校验字典类型称是否唯一
     *
     * @param sysDictTypeDTO 字典类型
     * @return 结果
     */
    boolean checkDictTypeUnique(SysDictTypeDTO sysDictTypeDTO);
}
