package com.mhd.boot.common.operatelog.core.service;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import com.mhd.boot.common.operatelog.core.annotation.OperateLog;
import com.mhd.boot.common.operatelog.core.enums.OperateResultEnum;
import com.mhd.boot.common.operatelog.core.vo.OperateLogVO;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;

/**
 * 操作日志收集器默认接口抽象类
 *
 * @author zhao-hao-dong
 **/
@Slf4j
public abstract class AbstractOperateLogDefaultCollectorService implements OperateLogCollectorService {
    @Override
    public OperateLogVO collectLog(ProceedingJoinPoint joinPoint, LocalDateTime startTime, Object result, Throwable exception) {
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

        OperateLogVO operateLogVO = new OperateLogVO();
        try {
            //operateLogVO.setUserId();
            //
            operateLogVO.setOperateModule(operateLog.module());
            operateLogVO.setOperateDescription(operateLog.description());
            // 如果注解中的类型不为空，则根据注解中类型填写操作类型。
            operateLogVO.setOperateType(operateLog.type().getType());
            if (exception != null) {
                operateLogVO.setOperateResult(OperateResultEnum.ERROR.getCode());
                operateLogVO.setOperateExceptionDetail(ExceptionUtil.stacktraceToString(exception).getBytes(StandardCharsets.UTF_8));
            } else {
                operateLogVO.setOperateResult(OperateResultEnum.SUCCESS.getCode());
            }
            // 设置方法名称
            String methodName = joinPoint.getTarget().getClass().getName() + "." + signature.getName() + "()";
            operateLogVO.setRequestMethod(methodName);
            // 处理请求参数
            // TODO 日志记录参数
            //if (operateLog.logArgs()) {
            //    operateLogVO.setRequestParams(MethodUtils.getParameter(method, joinPoint.getArgs()));
            //}
            //// 处理响应参数
            //if (operateLog.logResultData()) {
            //    operateLogVO.setRequestResult(JSON.toJSONString(result));
            //}
            //// 处理请求IP
            //operateLogVO.setRequestIp(IpUtils.getClientIp());
            //// 处理请求IP来源
            //operateLogVO.setRequestAddress(IpUtils.getCityInfo(operateLogVO.getRequestIp()));
            //// 处理浏览器类型
            //operateLogVO.setRequestBrowser(IpUtils.getBrowser());
            // 设置耗时
            operateLogVO.setDuration(LocalDateTimeUtil.between(startTime, LocalDateTime.now()).toMillis());
            operateLogVO.setOtherParams(new HashMap<>());
        } catch (Throwable ex) {
            log.error("[operateLog][默认收集日志时，发生异常，其中参数是 joinPoint({}) operateLog({}) result({}) exception({}) ]",
                    joinPoint, operateLog, result, exception, ex);
        }
        extractCollectLog(joinPoint, startTime, result, exception, operateLogVO);
        return operateLogVO;
    }

    public abstract void extractCollectLog(ProceedingJoinPoint joinPoint, LocalDateTime startTime, Object result, Throwable exception, OperateLogVO operateLogVO);
}
