package com.mhd.boot.common.operatelog.core.aspect;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import com.mhd.boot.common.operatelog.core.annotation.OperateLog;
import com.mhd.boot.common.operatelog.core.enums.OperateResultEnum;
import com.mhd.boot.common.operatelog.core.event.OperateLogEvent;
import com.mhd.boot.common.utils.SpringUtils;
import com.mhd.boot.common.utils.json.JsonUtils;
import com.mhd.boot.common.utils.servlet.ServletUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;

/**
 * 操作日志切面配置
 *
 * @author zhao-hao-dong
 **/
@Aspect
@Slf4j
public class OperateLogAspect {

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
        OperateLogEvent operateLogEvent = collectLog(joinPoint, startTime, result, exception);
        // 如果不记录日志，直接返回
        if (operateLogEvent == null) {
            return;
        }
        // 发布事件保存数据库
        SpringUtils.context().publishEvent(operateLogEvent);
    }

    private OperateLogEvent collectLog(ProceedingJoinPoint joinPoint, LocalDateTime startTime, Object result, Throwable exception) {
        // 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // 获取方法对象
        Method method = signature.getMethod();
        // 获取注解对象
        OperateLog operateLog = method.getAnnotation(OperateLog.class);
        // 如果不记录日志，直接返回
        if (operateLog == null || !operateLog.enable()) {
            return null;
        }
        OperateLogEvent operateLogEvent = new OperateLogEvent();
        try {
            //operateLogVO.setUserId();
            //
            operateLogEvent.setOperateModule(operateLog.module());
            operateLogEvent.setOperateDescription(operateLog.description());
            operateLogEvent.setOperateType(operateLog.type().getType());
            if (exception != null) {
                operateLogEvent.setOperateResult(OperateResultEnum.ERROR.getCode());
                operateLogEvent.setOperateExceptionDetail(ExceptionUtil.stacktraceToString(exception));
            } else {
                operateLogEvent.setOperateResult(OperateResultEnum.SUCCESS.getCode());
            }
            // 设置方法名称
            String methodName = joinPoint.getTarget().getClass().getName() + "." + signature.getName() + "()";
            operateLogEvent.setRequestMethod(methodName);
            // 处理请求参数
            if (operateLog.logArgs()) {
                //operateLogEvent.setRequestParams(MethodUtils.getParameter(method, joinPoint.getArgs()));
            }
            // 处理响应参数
            if (operateLog.logResultData()) {
                operateLogEvent.setRequestResult(JsonUtils.toJsonString(result));
            }
            // 处理请求IP
            operateLogEvent.setRequestIp(ServletUtils.getClientIp());
            // 处理请求IP来源
            //operateLogEvent.setRequestAddress(IpUtils.getCityInfo(operateLogEvent.getRequestIp()));
            // 处理浏览器类型
            //operateLogEvent.setRequestBrowser(IpUtils.getBrowser());
            // 设置耗时
            operateLogEvent.setDuration(LocalDateTimeUtil.between(startTime, LocalDateTime.now()).toMillis());
            operateLogEvent.setOtherParams(new HashMap<>());
        } catch (Throwable ex) {
            log.error("[operateLog][收集日志时，发生异常，其中参数是 joinPoint({}) operateLog({}) result({}) exception({}) ]",
                    joinPoint, operateLog, result, exception, ex);
        }
        return operateLogEvent;
    }
}
