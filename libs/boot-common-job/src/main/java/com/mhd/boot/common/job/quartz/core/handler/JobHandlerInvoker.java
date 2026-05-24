package com.mhd.boot.common.job.quartz.core.handler;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.mhd.boot.common.job.quartz.core.enums.JobDataKeyEnum;
import com.mhd.boot.common.job.quartz.core.service.JobLogHandlerService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.time.LocalDateTime;

/**
 * 基础 Job 调用者，负责调用 {@link JobHandler#execute(String)} 执行任务
 *
 * @author zhao-hao-dong
 **/
@PersistJobDataAfterExecution // 在成功执行定时任务之后，更新 JobDetail 中 JobDataMap 的数据
@DisallowConcurrentExecution // 禁止并发执行多个相同定义的 JobDetail
@Slf4j
public class JobHandlerInvoker extends QuartzJobBean {
    @Resource
    private JobLogHandlerService jobLogHandlerService;

    @Override
    protected void executeInternal(JobExecutionContext executionContext) throws JobExecutionException {
        // region 第一步：获取 Job 参数
        // 获取 Job ID
        Long jobId = executionContext.getMergedJobDataMap().getLong(JobDataKeyEnum.JOB_ID.name());
        // 获取 Job 处理器的名称
        String jobHandlerName = executionContext.getMergedJobDataMap().getString(JobDataKeyEnum.JOB_HANDLER_NAME.name());
        // 获取 Job 处理器的参数
        String jobHandlerParam = executionContext.getMergedJobDataMap().getString(JobDataKeyEnum.JOB_HANDLER_PARAM.name());
        int refireCount = executionContext.getRefireCount();
        // 获取重试次数，默认 0
        int retryCount = (Integer) executionContext.getMergedJobDataMap().getOrDefault(JobDataKeyEnum.JOB_RETRY_COUNT.name(), 0);
        // 获取重试间隔，默认 0
        int retryInterval = (Integer) executionContext.getMergedJobDataMap().getOrDefault(JobDataKeyEnum.JOB_RETRY_INTERVAL.name(), 0);
        // endregion

        // region 第二步：执行任务
        Long jobLogId = null;
        LocalDateTime startTime = LocalDateTime.now();
        String data = null;
        Throwable exception = null;
        try {
            // 记录 Job 日志
            jobLogId = jobLogHandlerService.createJobLog(jobId, startTime, jobHandlerName, jobHandlerParam, refireCount + 1);
            // 执行任务
            data = this.executeInternal(jobHandlerName, jobHandlerParam);
        } catch (Throwable ex) {
            exception = ex;
        }
        // endregion

        // region 第三步：记录执行日志
        this.updateJobLogResultAsync(jobLogId, startTime, data, exception, executionContext);
        // endregion

        // region 第四步：处理异常情况
        handleException(exception, refireCount, retryCount, retryInterval);
        // endregion
    }

    private String executeInternal(String jobHandlerName, String jobHandlerParam) throws Exception {
        // 获得 JobHandler 对象
        JobHandler jobHandler = SpringUtil.getBean(jobHandlerName, JobHandler.class);
        // 判断 JobHandler 是否为空
        Assert.notNull(jobHandler, "JobHandler 不会为空");
        // 执行任务
        return jobHandler.execute(jobHandlerParam);
    }

    private void updateJobLogResultAsync(Long jobLogId, LocalDateTime startTime, String data, Throwable exception, JobExecutionContext executionContext
    ) {
        LocalDateTime endTime = LocalDateTime.now();
        // 处理是否成功
        boolean success = exception == null;
        if (!success) {
            data = ExceptionUtil.getRootCauseMessage(exception);
        }
        // 更新日志
        try {
            jobLogHandlerService.updateJobLogResultAsync(jobLogId, endTime, (int) LocalDateTimeUtil.between(startTime, endTime).toMillis(), success, data);
        } catch (Exception ex) {
            log.error("[quartz][executeInternal][Job({}) logId({}) 记录执行日志失败({}/{})]",
                    executionContext.getJobDetail().getKey(), jobLogId, success, data);
        }
    }

    private void handleException(Throwable exception,
                                 int refireCount, int retryCount, int retryInterval) throws JobExecutionException {
        // 如果没有异常，直接返回
        if (exception == null) {
            return;
        }
        // 情况一：如果达到重试上限，则直接抛出异常
        if (refireCount >= retryCount) {
            throw new JobExecutionException(exception);
        }
        // 情况二：如果未到达重试上限，则 sleep 一定间隔时间，然后重试
        // 这里使用 sleep 来实现，主要还是希望实现比较简单。因为，同一时间，不会存在大量失败的 Job。
        if (retryInterval > 0) {
            ThreadUtil.sleep(retryInterval);
        }
        // 第二个参数，refireImmediately = true，表示立即重试
        throw new JobExecutionException(exception, true);

    }
}
