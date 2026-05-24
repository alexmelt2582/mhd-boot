package com.mhd.boot.common.job.quartz.core.service;

import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * @author zhao-hao-dong
 **/
@Slf4j
public class JobLogDefaultHandlerServiceImpl implements JobLogHandlerService {
    @Override
    public Long createJobLog(Long jobId, LocalDateTime beginTime, String jobHandlerName, String jobHandlerParam, Integer executeIndex) {
        log.info("[quartz][createJobLog][jobId:{}][beginTime:{}][jobHandlerName:{}][jobHandlerParam:{}][executeIndex:{}]", jobId, beginTime, jobHandlerName, jobHandlerParam, executeIndex);
        return IdUtil.getSnowflakeNextId();
    }

    @Override
    public void updateJobLogResultAsync(Long logId, LocalDateTime endTime, Integer duration, boolean success, String result) {
        log.info("[quartz][updateJobLog][logId:{}][endTime:{}][duration:{}][success:{}][result:{}]", logId, endTime, duration, success, result);
    }
}
