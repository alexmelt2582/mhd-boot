package com.mhd.boot.web.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mhd.boot.common.exception.BusinessException;
import com.mhd.boot.common.mybatis.core.domain.PageParam;
import com.mhd.boot.common.mybatis.core.domain.PageResponse;
import com.mhd.boot.common.mybatis.core.domain.PageResultUtils;
import com.mhd.boot.common.mybatis.core.utils.MybatisPlusUtils;
import com.mhd.boot.common.mybatis.core.wrapper.LambdaQueryWrapperX;
import com.mhd.boot.common.redis.utils.CacheUtils;
import com.mhd.boot.common.utils.MapstructUtils;
import com.mhd.boot.web.system.constant.CacheNames;
import com.mhd.boot.web.system.entity.SysDictData;
import com.mhd.boot.web.system.mapper.SysDictDataMapper;
import com.mhd.boot.web.system.model.dto.SysDictDataDTO;
import com.mhd.boot.web.system.model.vo.SysDictDataVo;
import com.mhd.boot.web.system.service.SysDictDataService;
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
public class SysDictDataServiceImpl implements SysDictDataService {
    private final SysDictDataMapper baseMapper;

    @Override
    public PageResponse<SysDictDataVo> selectPageDictDataList(SysDictDataDTO dto, PageParam pageParam) {
        Page<SysDictData> page = MybatisPlusUtils.buildPage(pageParam, null);
        LambdaQueryWrapperX<SysDictData> wrapperX = buildQueryWrapper(dto);
        IPage<SysDictDataVo> voPage = MybatisPlusUtils.selectVoPage(baseMapper, page, wrapperX, SysDictDataVo.class);
        return PageResultUtils.build(voPage);
    }

    @Override
    public List<SysDictDataVo> selectDictDataList(SysDictDataDTO dto) {
        LambdaQueryWrapperX<SysDictData> wrapperX = buildQueryWrapper(dto);
        List<SysDictData> list = baseMapper.selectList(wrapperX);
        return MapstructUtils.convert(list, SysDictDataVo.class);

    }

    private LambdaQueryWrapperX<SysDictData> buildQueryWrapper(SysDictDataDTO dto) {
        LambdaQueryWrapperX<SysDictData> lqw = new LambdaQueryWrapperX<>();
        lqw.likeIfPresent(SysDictData::getDictLabel, dto.getDictLabel());
        lqw.eqIfPresent(SysDictData::getDictType, dto.getDictType());
        lqw.orderByAsc(SysDictData::getDictSort, SysDictData::getDictCode);
        return lqw;
    }

    @Override
    public String selectDictLabel(String dictType, String dictValue) {
        return baseMapper.selectOne(new LambdaQueryWrapper<SysDictData>()
                        .select(SysDictData::getDictLabel)
                        .eq(SysDictData::getDictType, dictType)
                        .eq(SysDictData::getDictValue, dictValue))
                .getDictLabel();
    }

    @Override
    public SysDictDataVo selectDictDataById(Long dictCode) {
        SysDictData sysDictData = baseMapper.selectById(dictCode);
        return MapstructUtils.convert(sysDictData, SysDictDataVo.class);
    }

    @Override
    public void deleteDictDataByIds(List<Long> dictCodes) {
        List<SysDictData> list = baseMapper.selectByIds(dictCodes);
        baseMapper.deleteByIds(dictCodes);
        list.forEach(x -> CacheUtils.evict(CacheNames.SYS_DICT, x.getDictType()));
    }

    @CachePut(cacheNames = CacheNames.SYS_DICT, key = "#sysDictDataDTO.dictType")
    @Override
    public List<SysDictDataVo> insertDictData(SysDictDataDTO dto) {
        SysDictData data = MapstructUtils.convert(dto, SysDictData.class);
        int row = baseMapper.insert(data);
        if (row > 0) {
            List<SysDictData> list = baseMapper.selectDictDataByType(data.getDictType());
            return MapstructUtils.convert(list, SysDictDataVo.class);
        }
        throw new BusinessException("操作失败");
    }

    @CachePut(cacheNames = CacheNames.SYS_DICT, key = "#dto.dictType")
    @Override
    public List<SysDictDataVo> updateDictData(SysDictDataDTO dto) {
        SysDictData data = MapstructUtils.convert(dto, SysDictData.class);
        int row = baseMapper.updateById(data);
        if (row > 0) {
            List<SysDictData> list = baseMapper.selectDictDataByType(data.getDictType());
            return MapstructUtils.convert(list, SysDictDataVo.class);
        }
        throw new BusinessException("操作失败");
    }

    @Override
    public boolean checkDictDataUnique(SysDictDataDTO dto) {
        boolean exist = baseMapper.exists(new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getDictType, dto.getDictType())
                .eq(SysDictData::getDictValue, dto.getDictValue())
                .ne(ObjectUtil.isNotNull(dto.getDictCode()), SysDictData::getDictCode, dto.getDictCode()));
        return !exist;
    }
}
