package com.mhd.boot.web.system.service;

import com.mhd.boot.common.mybatis.core.domain.PageParam;
import com.mhd.boot.common.mybatis.core.domain.PageResponse;
import com.mhd.boot.web.system.entity.SysDictData;
import com.mhd.boot.web.system.model.vo.SysDictDataVo;

import java.util.List;

/**
 * 字典 业务层
 *
 * @author zhao-hao-dong
 */
public interface SysDictDataService {
    /**
     * 分页查询字典数据列表
     *
     * @param sysDictData  查询条件
     * @param pageParam 分页参数
     * @return 字典数据分页列表
     */
    PageResponse<SysDictDataVo> selectPageDictDataList(SysDictData sysDictData, PageParam pageParam);

    /**
     * 根据条件分页查询字典数据
     *
     * @param sysDictData 字典数据信息
     * @return 字典数据集合信息
     */
    List<SysDictDataVo> selectDictDataList(SysDictData sysDictData);

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
     * @param dictCode 字典数据ID
     * @return 字典数据
     */
    SysDictDataVo selectDictDataById(Long dictCode);

    /**
     * 批量删除字典数据信息
     *
     * @param dictCodes 需要删除的字典数据ID
     */
    void deleteDictDataByIds(List<Long> dictCodes);

    /**
     * 新增保存字典数据信息
     *
     * @param sysDictData 字典数据信息
     * @return 结果
     */
    List<SysDictDataVo> insertDictData(SysDictData sysDictData);

    /**
     * 修改保存字典数据信息
     *
     * @param sysDictData 字典数据信息
     * @return 结果
     */
    List<SysDictDataVo> updateDictData(SysDictData sysDictData);

    /**
     * 校验字典键值是否唯一
     *
     * @param sysDictData 字典数据
     * @return 结果
     */
    boolean checkDictDataUnique(SysDictData sysDictData);
}
