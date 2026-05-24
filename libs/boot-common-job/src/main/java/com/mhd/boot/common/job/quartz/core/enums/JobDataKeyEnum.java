package com.mhd.boot.common.job.quartz.core.enums;

/**
 * Job 数据 Key 枚举类
 *
 * @author zhao-hao-dong
 **/
public enum JobDataKeyEnum {
    /**
     * Job ID
     */
    JOB_ID,
    /**
     * Job 处理器的名称
     */
    JOB_HANDLER_NAME,
    /**
     * Job 处理器的参数
     */
    JOB_HANDLER_PARAM,
    /**
     * Job 最大重试次数
     */
    JOB_RETRY_COUNT,
    /**
     * Job 每次重试间隔
     */
    JOB_RETRY_INTERVAL,
}
