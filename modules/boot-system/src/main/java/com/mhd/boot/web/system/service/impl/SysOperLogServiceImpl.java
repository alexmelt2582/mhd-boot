package com.mhd.boot.web.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mhd.boot.common.mybatis.core.domain.PageInfo;
import com.mhd.boot.common.mybatis.core.domain.PageParam;
import com.mhd.boot.common.mybatis.core.domain.PageResultUtils;
import com.mhd.boot.common.mybatis.core.utils.MybatisPlusUtils;
import com.mhd.boot.common.mybatis.core.wrapper.LambdaQueryWrapperX;
import com.mhd.boot.common.operatelog.core.event.OperateLogEvent;
import com.mhd.boot.common.respnsedata.BaseResponse;
import com.mhd.boot.common.utils.MapstructUtils;
import com.mhd.boot.web.system.entity.SysOperLog;
import com.mhd.boot.web.system.mapper.SysOperLogMapper;
import com.mhd.boot.web.system.model.dto.SysOperLogDTO;
import com.mhd.boot.web.system.model.vo.SysOperLogVo;
import com.mhd.boot.web.system.service.SysOperLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * @author zhao-hao-dong
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SysOperLogServiceImpl implements SysOperLogService {
    private final SysOperLogMapper baseMapper;

    /**
     * 操作日志记录
     *
     * @param operLogEvent 操作日志事件
     */
    @Async
    @EventListener
    public void recordOperLog(OperateLogEvent operLogEvent) {
        log.info("record operate log: {}", operLogEvent);
        SysOperLogDTO operLog = MapstructUtils.convert(operLogEvent, SysOperLogDTO.class);
        insertOperLog(operLog);
    }

    @Override
    public BaseResponse<PageInfo<SysOperLogVo>> selectPageOperLogList(SysOperLogDTO dto, PageParam pageParam) {
        Page<SysOperLog> page = MybatisPlusUtils.buildPage(pageParam, null);
        LambdaQueryWrapperX<SysOperLog> wrapperX = buildQueryWrapper(dto);
        IPage<SysOperLogVo> voPage = MybatisPlusUtils.selectVoPage(baseMapper, page, wrapperX, SysOperLogVo.class);
        return PageResultUtils.build(voPage);
    }

    @Override
    public List<SysOperLogVo> selectOperLogList(SysOperLogDTO dto) {
        LambdaQueryWrapperX<SysOperLog> wrapperX = buildQueryWrapper(dto);
        List<SysOperLog> list = baseMapper.selectList(wrapperX);
        return MapstructUtils.convert(list, SysOperLogVo.class);
    }

    private LambdaQueryWrapperX<SysOperLog> buildQueryWrapper(SysOperLogDTO dto) {
        LambdaQueryWrapperX<SysOperLog> lqw = new LambdaQueryWrapperX<>();
        lqw.likeIfPresent(SysOperLog::getRequestIp, dto.getRequestIp());
        lqw.likeIfPresent(SysOperLog::getOperateModule, dto.getOperateModule());
        lqw.eqIfPresent(SysOperLog::getOperateType, dto.getOperateType());
        lqw.orderByDesc(SysOperLog::getCreateTime);
        return lqw;
    }

    @Override
    public SysOperLogVo selectLogById(Long operId) {
        SysOperLog sysOperLog = baseMapper.selectById(operId);
        return MapstructUtils.convert(sysOperLog, SysOperLogVo.class);
    }

    @Override
    public void insertOperLog(SysOperLogDTO dto) {
        SysOperLog operLog = MapstructUtils.convert(dto, SysOperLog.class);
        operLog.setCreateTime(LocalDateTime.now());
        baseMapper.insert(operLog);
    }

    @Override
    public int deleteOperLogByIds(Long[] operIds) {
        return baseMapper.deleteByIds(Arrays.asList(operIds));
    }

    @Override
    public void cleanOperLog() {
        baseMapper.delete(new LambdaQueryWrapper<>());
    }
}
