package com.mhd.boot.web.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mhd.boot.common.exception.BusinessException;
import com.mhd.boot.common.mybatis.core.domain.PageInfo;
import com.mhd.boot.common.mybatis.core.domain.PageParam;
import com.mhd.boot.common.mybatis.core.domain.PageResultUtils;
import com.mhd.boot.common.mybatis.core.utils.MybatisPlusUtils;
import com.mhd.boot.common.mybatis.core.wrapper.LambdaQueryWrapperX;
import com.mhd.boot.common.redis.utils.CacheUtils;
import com.mhd.boot.common.responsedata.BaseResponse;
import com.mhd.boot.common.utils.MapstructUtils;
import com.mhd.boot.web.system.constant.CacheNames;
import com.mhd.boot.web.system.entity.SysDictItem;
import com.mhd.boot.web.system.mapper.SysDictItemMapper;
import com.mhd.boot.web.system.model.dto.SysDictItemDTO;
import com.mhd.boot.web.system.model.vo.SysDictItemVo;
import com.mhd.boot.web.system.service.SysDictItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author zhao-hao-dong
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SysDictItemServiceImpl implements SysDictItemService {
    private final SysDictItemMapper baseMapper;

    @Override
    public BaseResponse<PageInfo<SysDictItemVo>> selectPageDictItemList(SysDictItemDTO dto, PageParam pageParam) {
        Page<SysDictItem> page = MybatisPlusUtils.buildPage(pageParam, null);
        LambdaQueryWrapperX<SysDictItem> wrapperX = buildQueryWrapper(dto);
        IPage<SysDictItemVo> voPage = MybatisPlusUtils.selectVoPage(baseMapper, page, wrapperX, SysDictItemVo.class);
        return PageResultUtils.build(voPage);
    }

    @Override
    public List<SysDictItemVo> selectDictItemList(SysDictItemDTO dto) {
        LambdaQueryWrapperX<SysDictItem> wrapperX = buildQueryWrapper(dto);
        List<SysDictItem> list = baseMapper.selectList(wrapperX);
        return MapstructUtils.convert(list, SysDictItemVo.class);

    }

    private LambdaQueryWrapperX<SysDictItem> buildQueryWrapper(SysDictItemDTO dto) {
        LambdaQueryWrapperX<SysDictItem> lqw = new LambdaQueryWrapperX<>();
        lqw.likeIfPresent(SysDictItem::getDictLabel, dto.getDictLabel());
        lqw.eqIfPresent(SysDictItem::getDictType, dto.getDictType());
        lqw.orderByAsc(SysDictItem::getDictSort, SysDictItem::getDictItemId);
        return lqw;
    }

    @Override
    public String selectDictLabel(String dictType, String dictValue) {
        return baseMapper.selectOne(new LambdaQueryWrapper<SysDictItem>()
                        .select(SysDictItem::getDictLabel)
                        .eq(SysDictItem::getDictType, dictType)
                        .eq(SysDictItem::getDictValue, dictValue))
                .getDictLabel();
    }

    @Override
    public SysDictItemVo selectDictItemById(Long dictItemId) {
        SysDictItem sysDictItem = baseMapper.selectById(dictItemId);
        return MapstructUtils.convert(sysDictItem, SysDictItemVo.class);
    }

    @Override
    public void deleteDictItemByIds(List<Long> dictItemIds) {
        List<SysDictItem> list = baseMapper.selectByIds(dictItemIds);
        baseMapper.deleteByIds(dictItemIds);
        list.forEach(x -> CacheUtils.evict(CacheNames.SYS_DICT, x.getDictType()));
    }

    @CachePut(cacheNames = CacheNames.SYS_DICT, key = "#dto.dictType")
    @Override
    public List<SysDictItemVo> insertDictItem(SysDictItemDTO dto) {
        SysDictItem data = MapstructUtils.convert(dto, SysDictItem.class);
        int row = baseMapper.insert(data);
        if (row > 0) {
            List<SysDictItem> list = baseMapper.selectDictItemListByType(data.getDictType());
            return MapstructUtils.convert(list, SysDictItemVo.class);
        }
        throw new BusinessException("操作失败");
    }

    @CachePut(cacheNames = CacheNames.SYS_DICT, key = "#dto.dictType")
    @Override
    public List<SysDictItemVo> updateDictItem(SysDictItemDTO dto) {
        SysDictItem item = MapstructUtils.convert(dto, SysDictItem.class);
        int row = baseMapper.updateById(item);
        if (row > 0) {
            List<SysDictItem> list = baseMapper.selectDictItemListByType(item.getDictType());
            return MapstructUtils.convert(list, SysDictItemVo.class);
        }
        throw new BusinessException("操作失败");
    }

    @Override
    public boolean checkDictItemUnique(SysDictItemDTO dto) {
        boolean exist = baseMapper.exists(new LambdaQueryWrapper<SysDictItem>()
                .eq(SysDictItem::getDictType, dto.getDictType())
                .eq(SysDictItem::getDictValue, dto.getDictValue())
                .ne(ObjectUtil.isNotNull(dto.getDictItemId()), SysDictItem::getDictItemId, dto.getDictItemId()));
        return !exist;
    }
}
