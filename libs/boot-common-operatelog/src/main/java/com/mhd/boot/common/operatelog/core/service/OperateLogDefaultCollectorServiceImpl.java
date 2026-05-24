package com.mhd.boot.common.operatelog.core.service;

import com.mhd.plugin.operatelog.core.vo.OperateLogVO;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;

import java.time.LocalDateTime;

/**
 * 操作日志收集器默认接口实现类
 *
 * @author zhao-hao-dong
 **/
@Slf4j
public class OperateLogDefaultCollectorServiceImpl extends AbstractOperateLogDefaultCollectorService {

    @Override
    public void extractCollectLog(ProceedingJoinPoint joinPoint, LocalDateTime startTime, Object result, Throwable exception, OperateLogVO operateLogVO) {
        // do nothing
    }
}
