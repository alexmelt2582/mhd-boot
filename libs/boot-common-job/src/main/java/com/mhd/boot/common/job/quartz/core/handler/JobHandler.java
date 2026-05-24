package com.mhd.boot.common.job.quartz.core.handler;

/**
 * 任务处理器接口
 *
 * @author zhao-hao-dong
 **/
public interface JobHandler {
    /**
     * 执行任务
     *
     * @param params 参数
     * @return 结果
     * @throws Exception 异常
     */
    String execute(String params) throws Exception;
}
