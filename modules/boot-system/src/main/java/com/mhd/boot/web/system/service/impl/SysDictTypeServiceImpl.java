package com.mhd.boot.web.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mhd.boot.common.enums.ErrorCodeEnum;
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
import com.mhd.boot.web.system.entity.SysDictType;
import com.mhd.boot.web.system.mapper.SysDictDataMapper;
import com.mhd.boot.web.system.mapper.SysDictTypeMapper;
import com.mhd.boot.web.system.model.dto.SysDictTypeDTO;
import com.mhd.boot.web.system.model.vo.SysDictDataVo;
import com.mhd.boot.web.system.model.vo.SysDictTypeVo;
import com.mhd.boot.web.system.service.SysDictTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhao-hao-dong
 */
@RequiredArgsConstructor
@Service
public class SysDictTypeServiceImpl implements SysDictTypeService {
    private final SysDictTypeMapper baseMapper;
    private final SysDictDataMapper dictDataMapper;

    @Override
    public PageResponse<SysDictTypeVo> selectPageDictTypeList(SysDictTypeDTO sysDictTypeDTO, PageParam pageParam) {
        Page<SysDictType> page = MybatisPlusUtils.buildPage(pageParam, null);
        LambdaQueryWrapperX<SysDictType> wrapperX = buildQueryWrapper(sysDictTypeDTO);
        IPage<SysDictTypeVo> voPage = MybatisPlusUtils.selectVoPage(baseMapper, page, wrapperX, SysDictTypeVo.class);
        return PageResultUtils.build(voPage);
    }

    @Override
    public List<SysDictTypeVo> selectPageDictTypeList(SysDictTypeDTO sysDictTypeDTO) {
        LambdaQueryWrapperX<SysDictType> wrapperX = buildQueryWrapper(sysDictTypeDTO);
        List<SysDictType> sysDictTypeList = baseMapper.selectList(wrapperX);
        return MapstructUtils.convert(sysDictTypeList, SysDictTypeVo.class);
    }

    private LambdaQueryWrapperX<SysDictType> buildQueryWrapper(SysDictTypeDTO sysDictTypeDTO) {
        LambdaQueryWrapperX<SysDictType> lqw = new LambdaQueryWrapperX<>();
        lqw.likeIfPresent(SysDictType::getDictName, sysDictTypeDTO.getDictName());
        lqw.likeIfPresent(SysDictType::getDictType, sysDictTypeDTO.getDictType());
        lqw.orderByAsc(SysDictType::getId);
        return lqw;
    }

    @Override
    public List<SysDictTypeVo> selectDictTypeAll() {
        List<SysDictType> sysDictTypes = baseMapper.selectList(null);
        return MapstructUtils.convert(sysDictTypes, SysDictTypeVo.class);
    }

    @Cacheable(cacheNames = CacheNames.SYS_DICT, key = "#dictType")
    @Override
    public List<SysDictDataVo> selectDictDataByType(String dictType) {
        List<SysDictData> dictDatas = dictDataMapper.selectDictDataByType(dictType);
        return MapstructUtils.convert(dictDatas, SysDictDataVo.class);
    }

    @Override
    public SysDictTypeVo selectDictTypeById(Long id) {
        SysDictType sysDictType = baseMapper.selectById(id);
        return MapstructUtils.convert(sysDictType, SysDictTypeVo.class);
    }

    @Cacheable(cacheNames = CacheNames.SYS_DICT_TYPE, key = "#dictType")
    @Override
    public SysDictTypeVo selectDictTypeByType(String dictType) {
        SysDictType sysDictType = baseMapper.selectOne(new LambdaQueryWrapperX<SysDictType>().eq(SysDictType::getDictType, dictType));
        return MapstructUtils.convert(sysDictType, SysDictTypeVo.class);
    }

    @Override
    public void deleteDictTypeByIds(List<Long> ids) {
        List<SysDictType> list = baseMapper.selectByIds(ids);
        list.forEach(x -> {
            boolean assigned = dictDataMapper.exists(new LambdaQueryWrapper<SysDictData>()
                    .eq(SysDictData::getDictType, x.getDictType()));
            if (assigned) {
                throw new BusinessException("{}已分配,不能删除", x.getDictName());
            }
        });
        baseMapper.deleteByIds(ids);
        list.forEach(x -> {
            CacheUtils.evict(CacheNames.SYS_DICT, x.getDictType());
            CacheUtils.evict(CacheNames.SYS_DICT_TYPE, x.getDictType());
        });
    }

    @Override
    public void resetDictCache() {
        CacheUtils.clear(CacheNames.SYS_DICT);
        CacheUtils.clear(CacheNames.SYS_DICT_TYPE);
    }

    @CachePut(cacheNames = CacheNames.SYS_DICT, key = "#sysDictTypeDTO.dictType")
    @Override
    public List<SysDictTypeVo> insertDictType(SysDictTypeDTO sysDictTypeDTO) {
        SysDictType dict = MapstructUtils.convert(sysDictTypeDTO, SysDictType.class);
        int row = baseMapper.insert(dict);
        if (row > 0) {
            // 新增 type 下无 data 数据 返回空防止缓存穿透
            return new ArrayList<>();
        }
        throw new BusinessException(ErrorCodeEnum.FAIL);
    }

    @CachePut(cacheNames = CacheNames.SYS_DICT, key = "#sysDictTypeDTO.dictType")
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<SysDictDataVo> updateDictType(SysDictTypeDTO sysDictTypeDTO) {
        SysDictType dict = MapstructUtils.convert(sysDictTypeDTO, SysDictType.class);
        SysDictType oldDict = baseMapper.selectById(dict.getId());
        dictDataMapper.update(null, new LambdaUpdateWrapper<SysDictData>()
                .set(SysDictData::getDictType, dict.getDictType())
                .eq(SysDictData::getDictType, oldDict.getDictType()));
        int row = baseMapper.updateById(dict);
        if (row > 0) {
            CacheUtils.evict(CacheNames.SYS_DICT, oldDict.getDictType());
            CacheUtils.evict(CacheNames.SYS_DICT_TYPE, oldDict.getDictType());
            List<SysDictData> sysDictData = dictDataMapper.selectDictDataByType(dict.getDictType());
            return MapstructUtils.convert(sysDictData, SysDictDataVo.class);
        }
        throw new BusinessException(ErrorCodeEnum.FAIL);
    }

    @Override
    public boolean checkDictTypeUnique(SysDictTypeDTO sysDictTypeDTO) {
        LambdaQueryWrapperX<SysDictType> lqx = new LambdaQueryWrapperX<>();
        lqx.eq(SysDictType::getDictType, sysDictTypeDTO.getDictType());
        lqx.neIfPresent(SysDictType::getId, sysDictTypeDTO.getId());
        boolean exist = baseMapper.exists(lqx);
        return !exist;
    }
}
