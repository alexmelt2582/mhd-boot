package com.mhd.boot.web.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mhd.boot.common.enums.ErrorCodeEnum;
import com.mhd.boot.common.exception.BusinessException;
import com.mhd.boot.common.mybatis.core.domain.PageInfo;
import com.mhd.boot.common.mybatis.core.domain.PageParam;
import com.mhd.boot.common.mybatis.core.domain.PageResultUtils;
import com.mhd.boot.common.mybatis.core.utils.MybatisPlusUtils;
import com.mhd.boot.common.mybatis.core.wrapper.LambdaQueryWrapperX;
import com.mhd.boot.common.redis.utils.CacheUtils;
import com.mhd.boot.common.responsedata.BaseResponse;
import com.mhd.boot.common.utils.MapstructUtils;
import com.mhd.boot.common.utils.SpringUtils;
import com.mhd.boot.common.utils.StringUtils;
import com.mhd.boot.common.utils.collection.CollectionUtils;
import com.mhd.boot.common.dto.DictItemDTO;
import com.mhd.boot.common.dto.DictTypeDTO;
import com.mhd.boot.common.service.DictService;
import com.mhd.boot.web.system.constant.CacheNames;
import com.mhd.boot.web.system.entity.SysDictItem;
import com.mhd.boot.web.system.entity.SysDictType;
import com.mhd.boot.web.system.mapper.SysDictItemMapper;
import com.mhd.boot.web.system.mapper.SysDictTypeMapper;
import com.mhd.boot.web.system.model.dto.SysDictTypeDTO;
import com.mhd.boot.web.system.model.vo.SysDictItemVo;
import com.mhd.boot.web.system.model.vo.SysDictTypeVo;
import com.mhd.boot.web.system.service.SysDictTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class SysDictTypeServiceImpl implements SysDictTypeService, DictService {
    private final SysDictTypeMapper baseMapper;
    private final SysDictItemMapper dictItemMapper;

    @Override
    public BaseResponse<PageInfo<SysDictTypeVo>> selectPageDictTypeList(SysDictTypeDTO dto, PageParam pageParam) {
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
    public List<SysDictItemVo> selectDictDataByType(String dictType) {
        List<SysDictItem> dictDatas = dictItemMapper.selectDictItemListByType(dictType);
        return MapstructUtils.convert(dictDatas, SysDictItemVo.class);
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
            boolean assigned = dictItemMapper.exists(new LambdaQueryWrapper<SysDictItem>()
                    .eq(SysDictItem::getDictType, x.getDictType()));
            if (assigned) {
                throw new BusinessException("{}已分配,不能删除", x.getDictName());
            }
        });
        baseMapper.deleteByIds(dictIds);
        list.forEach(x -> {
            CacheUtils.evict(CacheNames.SYS_DICT, x.getDictType());
            CacheUtils.evict(CacheNames.SYS_DICT_TYPE, x.getDictType());
        });
        log.info("delete dict type success, dictIds: {}, dictTypes: {}", dictIds, CollectionUtils.convertList(list, SysDictType::getDictType));
        log.info("delete dict type cache success, dictTypes: {}", CollectionUtils.convertList(list, SysDictType::getDictType));
    }

    @Override
    public void resetDictCache() {
        CacheUtils.clear(CacheNames.SYS_DICT);
        CacheUtils.clear(CacheNames.SYS_DICT_TYPE);
        log.info("clear dict cache success");
    }

    @CachePut(cacheNames = CacheNames.SYS_DICT, key = "#sysDictTypeDTO.dictType")
    @Override
    public List<SysDictTypeVo> insertDictType(SysDictTypeDTO sysDictTypeDTO) {
        SysDictType dict = MapstructUtils.convert(sysDictTypeDTO, SysDictType.class);
        int row = baseMapper.insert(dict);
        if (row > 0) {
            log.info("add dictType success,dictType:{}", dict.getDictType());
            // 新增 type 下无 data 数据 返回空防止缓存穿透
            return new ArrayList<>();
        }
        throw new BusinessException(ErrorCodeEnum.FAIL);
    }

    @CachePut(cacheNames = CacheNames.SYS_DICT, key = "#sysDictTypeDTO.dictType")
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<SysDictItemVo> updateDictType(SysDictTypeDTO sysDictTypeDTO) {
        SysDictType dict = MapstructUtils.convert(sysDictTypeDTO, SysDictType.class);
        SysDictType oldDict = baseMapper.selectById(dict.getDictId());
        dictItemMapper.update(null, new LambdaUpdateWrapper<SysDictItem>()
                .set(SysDictItem::getDictType, dict.getDictType())
                .eq(SysDictItem::getDictType, oldDict.getDictType()));
        int row = baseMapper.updateById(dict);
        if (row > 0) {
            log.info("update dictType success,dictType:{}", dict.getDictType());
            CacheUtils.evict(CacheNames.SYS_DICT, oldDict.getDictType());
            CacheUtils.evict(CacheNames.SYS_DICT_TYPE, oldDict.getDictType());
            List<SysDictItem> sysDictData = dictItemMapper.selectDictItemListByType(dict.getDictType());
            return MapstructUtils.convert(sysDictData, SysDictItemVo.class);
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
        List<SysDictItemVo> datas = SpringUtils.getAopProxy(this).selectDictDataByType(dictType);
        Map<String, String> map = CollectionUtils.convertMap(datas, SysDictItemVo::getDictValue, SysDictItemVo::getDictLabel);
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
        List<SysDictItemVo> datas = SpringUtils.getAopProxy(this).selectDictDataByType(dictType);
        Map<String, String> map = CollectionUtils.convertMap(datas, SysDictItemVo::getDictLabel, SysDictItemVo::getDictValue);
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
        List<SysDictItemVo> list = SpringUtils.getAopProxy(this).selectDictDataByType(dictType);
        // 保证顺序
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        for (SysDictItemVo vo : list) {
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
    public List<DictItemDTO> getDictItem(String dictType) {
        List<SysDictItemVo> list = SpringUtils.getAopProxy(this).selectDictDataByType(dictType);
        return BeanUtil.copyToList(list, DictItemDTO.class);
    }
}
