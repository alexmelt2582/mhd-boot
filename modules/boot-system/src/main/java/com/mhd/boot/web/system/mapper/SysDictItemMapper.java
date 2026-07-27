package com.mhd.boot.web.system.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mhd.boot.web.system.entity.SysDictItem;

import java.util.List;

/**
 * 字典表 数据层
 *
 * @author zhao-hao-dong
 */
public interface SysDictItemMapper extends BaseMapper<SysDictItem> {
    /**
     * 根据字典类型查询字典数据列表
     *
     * @param dictType 字典类型
     * @return 符合条件的字典数据列表
     */
    default List<SysDictItem> selectDictItemListByType(String dictType) {
        LambdaQueryWrapper<SysDictItem> queryWrapper = new LambdaQueryWrapper<SysDictItem>()
                .eq(SysDictItem::getDictType, dictType)
                .orderByAsc(SysDictItem::getDictSort);
        return this.selectList(queryWrapper);
    }
}
