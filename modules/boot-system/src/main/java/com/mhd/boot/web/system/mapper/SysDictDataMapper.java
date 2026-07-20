package com.mhd.boot.web.system.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mhd.boot.web.system.entity.SysDictData;

import java.util.List;

/**
 * 字典表 数据层
 *
 * @author zhao-hao-dong
 */
public interface SysDictDataMapper extends BaseMapper<SysDictData> {
    /**
     * 根据字典类型查询字典数据列表
     *
     * @param dictType 字典类型
     * @return 符合条件的字典数据列表
     */
    default List<SysDictData> selectDictDataByType(String dictType) {
        LambdaQueryWrapper<SysDictData> queryWrapper = new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getDictType, dictType)
                .orderByAsc(SysDictData::getDictSort);
        return this.selectList(queryWrapper);
    }
}
