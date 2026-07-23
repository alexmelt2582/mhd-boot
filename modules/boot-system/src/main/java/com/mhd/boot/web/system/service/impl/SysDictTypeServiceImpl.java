package com.mhd.boot.web.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
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
import com.mhd.boot.common.utils.SpringUtils;
import com.mhd.boot.common.utils.StringUtils;
import com.mhd.boot.common.utils.collection.CollectionUtils;
import com.mhd.boot.common.web.dto.DictDataDTO;
import com.mhd.boot.common.web.dto.DictTypeDTO;
import com.mhd.boot.common.web.service.DictService;
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

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhao-hao-dong
 */
@RequiredArgsConstructor
@Service
public class SysDictTypeServiceImpl implements SysDictTypeService, DictService {
    private final SysDictTypeMapper baseMapper;
    private final SysDictDataMapper dictDataMapper;

    @Override
    public PageResponse<SysDictTypeVo> selectPageDictTypeList(SysDictTypeDTO dto, PageParam pageParam) {
        Page<SysDictType> page = MybatisPlusUtils.buildPage(pageParam, null);
        LambdaQueryWrapperX<SysDictType> wrapperX = buildQueryWrapper(dto);
        IPage<SysDictTypeVo> voPage = MybatisPlusUtils.selectVoPage(baseMapper, page, wrapperX, SysDictTypeVo.class);
        return PageResultUtils.build(voPage);
    }

    @Override
    public List<SysDictTypeVo> selectPageDictTypeList(SysDictTypeDTO dto) {
        LambdaQueryWrapperX<SysDictType> wrapperX = buildQueryWrapper(dto);
        List<SysDictType> list = baseMapper.selectList(wrapperX);
        return MapstructUtils.convert(list, SysDictTypeVo.class);
    }

    private LambdaQueryWrapperX<SysDictType> buildQueryWrapper(SysDictTypeDTO dto) {
        LambdaQueryWrapperX<SysDictType> lqw = new LambdaQueryWrapperX<>();
        lqw.likeIfPresent(SysDictType::getDictName, dto.getDictName());
        lqw.likeIfPresent(SysDictType::getDictType, dto.getDictType());
        lqw.orderByAsc(SysDictType::getDictId);
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
    public SysDictTypeVo selectDictTypeById(Long dictId) {
        SysDictType sysDictType = baseMapper.selectById(dictId);
        return MapstructUtils.convert(sysDictType, SysDictTypeVo.class);
    }

    @Cacheable(cacheNames = CacheNames.SYS_DICT_TYPE, key = "#dictType")
    @Override
    public SysDictTypeVo selectDictTypeByType(String dictType) {
        SysDictType sysDictType = baseMapper.selectOne(new LambdaQueryWrapperX<SysDictType>().eq(SysDictType::getDictType, dictType));
        return MapstructUtils.convert(sysDictType, SysDictTypeVo.class);
    }

    @Override
    public void deleteDictTypeByIds(List<Long> dictIds) {
        List<SysDictType> list = baseMapper.selectByIds(dictIds);
        list.forEach(x -> {
            boolean assigned = dictDataMapper.exists(new LambdaQueryWrapper<SysDictData>()
                    .eq(SysDictData::getDictType, x.getDictType()));
            if (assigned) {
                throw new BusinessException("{}已分配,不能删除", x.getDictName());
            }
        });
        baseMapper.deleteByIds(dictIds);
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
        SysDictType oldDict = baseMapper.selectById(dict.getDictId());
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
        lqx.neIfPresent(SysDictType::getDictId, sysDictTypeDTO.getDictId());
        boolean exist = baseMapper.exists(lqx);
        return !exist;
    }

    @Override
    public String getDictLabel(String dictType, String dictValue, String separator) {
        List<SysDictDataVo> datas = SpringUtils.getAopProxy(this).selectDictDataByType(dictType);
        Map<String, String> map = CollectionUtils.convertMap(datas, SysDictDataVo::getDictValue, SysDictDataVo::getDictLabel);
        if (StringUtils.containsAny(dictValue, separator)) {
            return Arrays.stream(dictValue.split(separator))
                    .map(v -> map.getOrDefault(v, StringUtils.EMPTY))
                    .collect(Collectors.joining(separator));
        } else {
            return map.getOrDefault(dictValue, StringUtils.EMPTY);
        }
    }

    @Override
    public String getDictValue(String dictType, String dictLabel, String separator) {
        List<SysDictDataVo> datas = SpringUtils.getAopProxy(this).selectDictDataByType(dictType);
        Map<String, String> map = CollectionUtils.convertMap(datas, SysDictDataVo::getDictLabel, SysDictDataVo::getDictValue);
        if (StringUtils.containsAny(dictLabel, separator)) {
            return Arrays.stream(dictLabel.split(separator))
                    .map(l -> map.getOrDefault(l, StringUtils.EMPTY))
                    .collect(Collectors.joining(separator));
        } else {
            return map.getOrDefault(dictLabel, StringUtils.EMPTY);
        }
    }

    @Override
    public Map<String, String> getAllDictByDictType(String dictType) {
        List<SysDictDataVo> list = SpringUtils.getAopProxy(this).selectDictDataByType(dictType);
        // 保证顺序
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        for (SysDictDataVo vo : list) {
            map.put(vo.getDictValue(), vo.getDictLabel());
        }
        return map;
    }

    @Override
    public DictTypeDTO getDictType(String dictType) {
        SysDictTypeVo vo = SpringUtils.getAopProxy(this).selectDictTypeByType(dictType);
        return BeanUtil.toBean(vo, DictTypeDTO.class);
    }

    @Override
    public List<DictDataDTO> getDictData(String dictType) {
        List<SysDictDataVo> list = SpringUtils.getAopProxy(this).selectDictDataByType(dictType);
        return BeanUtil.copyToList(list, DictDataDTO.class);
    }
}
