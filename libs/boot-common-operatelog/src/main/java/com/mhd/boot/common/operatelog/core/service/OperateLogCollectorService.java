package com.mhd.boot.common.operatelog.core.service;

import com.mhd.boot.common.operatelog.core.vo.OperateLogVO;
import jakarta.validation.constraints.NotNull;
import org.aspectj.lang.ProceedingJoinPoint;

import java.time.LocalDateTime;

/**
 * 操作日志收集器接口
 *
 * @author zhao-hao-dong
 **/
public interface OperateLogCollectorService {
    /**
     * 收集日志
     *
     * @param joinPoint 切点
     * @param startTime 开始时间
     * @param result    执行结果
     * @param exception 异常
     * @return 日志 OperateLogVO 对象
     */
    OperateLogVO collectLog(@NotNull(message = "切点不能为空") ProceedingJoinPoint joinPoint,
                            @NotNull(message = "开始时间不能为空") LocalDateTime startTime,
                            Object result,
                            Throwable exception);
}
