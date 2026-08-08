package com.mhd.alert.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mhd.alert.entity.AlertEvent;
import com.mhd.alert.mapper.AlertEventMapper;
import com.mhd.alert.model.dto.AlertEventQueryDTO;
import com.mhd.alert.model.dto.AlertEventSaveDTO;
import com.mhd.alert.model.vo.AlertEventVo;
import com.mhd.alert.service.AlertEventService;
import com.mhd.boot.common.mybatis.core.domain.PageInfo;
import com.mhd.boot.common.mybatis.core.domain.PageParam;
import com.mhd.boot.common.mybatis.core.domain.PageResultUtils;
import com.mhd.boot.common.mybatis.core.utils.MybatisPlusUtils;
import com.mhd.boot.common.mybatis.core.wrapper.LambdaQueryWrapperX;
import com.mhd.boot.common.responsedata.BaseResponse;
import com.mhd.boot.common.utils.MapstructUtils;
import com.mhd.boot.common.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 告警事件 - Service实现层
 *
 * @author zhao-hao-dong
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AlertEventServiceImpl implements AlertEventService {
    private final AlertEventMapper baseMapper;

    @Override
    public BaseResponse<PageInfo<AlertEventVo>> selectPageList(AlertEventQueryDTO queryDTO, PageParam pageParam) {
        Page<AlertEvent> page = MybatisPlusUtils.buildPage(pageParam, null);
        LambdaQueryWrapperX<AlertEvent> wrapperX = buildQueryWrapper(queryDTO);
        IPage<AlertEventVo> voPage = MybatisPlusUtils.selectVoPage(baseMapper, page, wrapperX, AlertEventVo.class);
        return PageResultUtils.build(voPage);
    }

    @Override
    public List<AlertEventVo> selectList(AlertEventQueryDTO queryDTO) {
        LambdaQueryWrapperX<AlertEvent> wrapperX = buildQueryWrapper(queryDTO);
        List<AlertEvent> list = baseMapper.selectList(wrapperX);
        return MapstructUtils.convert(list, AlertEventVo.class);
    }

    private LambdaQueryWrapperX<AlertEvent> buildQueryWrapper(AlertEventQueryDTO queryDTO) {
        LambdaQueryWrapperX<AlertEvent> lqw = new LambdaQueryWrapperX<>();
        return lqw;
    }

    @Override
    public List<AlertEvent> selectListByStatus(String status) {
        LambdaQueryWrapperX<AlertEvent> lqw = new LambdaQueryWrapperX<>();
        lqw.eq(AlertEvent::getStatus, status);
        return baseMapper.selectList(lqw);
    }

    @Override
    public AlertEventVo selectById(Long id) {
        AlertEvent alertEvent = baseMapper.selectById(id);
        return MapstructUtils.convert(alertEvent, AlertEventVo.class);
    }

    @Override
    public AlertEvent selectByFingerprint(String fingerprint) {
        if (StringUtils.isBlank(fingerprint)) return null;
        LambdaQueryWrapperX<AlertEvent> lqw = new LambdaQueryWrapperX<>();
        lqw.eq(AlertEvent::getFingerprint, fingerprint);
        return baseMapper.selectOne(lqw);
    }

    @Override
    public int insertByDTO(AlertEventSaveDTO saveDTO) {
        validSaveDTO(saveDTO);
        AlertEvent alertEvent = MapstructUtils.convert(saveDTO, AlertEvent.class);
        return baseMapper.insert(alertEvent);
    }

    @Override
    public int updateByDTO(AlertEventSaveDTO saveDTO) {
        validSaveDTO(saveDTO);
        AlertEvent alertEvent = MapstructUtils.convert(saveDTO, AlertEvent.class);
        return baseMapper.updateById(alertEvent);
    }

    @Override
    public int deleteByIds(Long[] ids) {
        return baseMapper.deleteByIds(Arrays.asList(ids));
    }

    private void validSaveDTO(AlertEventSaveDTO saveDTO) {
        // TODO 进行校验
    }
}
