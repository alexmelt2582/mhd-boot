package com.mhd.boot.common.operatelog.core.aspect;

import com.mhd.boot.common.operatelog.core.service.OperateLogCollectorService;
import com.mhd.boot.common.operatelog.core.service.OperateLogHandlerService;
import com.mhd.boot.common.operatelog.core.vo.OperateLogVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.time.LocalDateTime;

/**
 * 操作日志切面配置
 *
 * @author zhao-hao-dong
 **/
@Aspect
@Slf4j
public class OperateLogAspect {
    @Resource
    private OperateLogHandlerService operateLogHandlerService;
    @Resource
    private OperateLogCollectorService operateLogCollectorService;

    /**
     * 以自定义的日志注解作为切入点
     */
    @Pointcut("@annotation(com.mhd.boot.common.operatelog.core.annotation.OperateLog)")
    public void logPointCut() {
    }

    @Around("logPointCut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        // 记录开始时间
        LocalDateTime startTime = LocalDateTime.now();
        try {
            // 执行原有方法
            Object result = joinPoint.proceed();
            // 记录正常执行时的操作日志
            this.handleOperateLog(joinPoint, startTime, result, null);
            return result;
        } catch (Throwable exception) {
            this.handleOperateLog(joinPoint, startTime, null, exception);
            throw exception;
        }
    }

    /**
     * 处理操作日志
     *
     * @param joinPoint 切点
     * @param startTime 开始时间
     * @param result    执行结果
     * @param exception 异常
     */
    private void handleOperateLog(ProceedingJoinPoint joinPoint, LocalDateTime startTime, Object result, Throwable exception) {
        OperateLogVO operateLogVO = operateLogCollectorService.collectLog(joinPoint, startTime, result, exception);
        // 如果不记录日志，直接返回
        if (operateLogVO == null) {
            return;
        }
        operateLogHandlerService.handleLog(operateLogVO);
    }
}
