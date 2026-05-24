package com.mhd.boot.common.job.quartz.config;

import cn.hutool.core.collection.CollUtil;
import com.mhd.boot.common.job.quartz.core.scheduler.SchedulerManager;
import com.mhd.boot.common.job.quartz.core.service.JobLogDefaultHandlerServiceImpl;
import com.mhd.boot.common.job.quartz.core.service.JobLogHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Optional;

/**
 * Quartz自动配置类
 *
 * @author zhao-hao-dong
 **/
@AutoConfiguration
@EnableScheduling // 开启 Spring 自带的定时任务
@Slf4j
public class MeQuartzAutoConfiguration {
    @Bean
    public ApplicationRunner quartzStatusRunner(Optional<Scheduler> scheduler) {
        return args -> {
            if (scheduler.isPresent()) {
                if (CollUtil.isNotEmpty(scheduler.get().getCurrentlyExecutingJobs())) {
                    log.info("[quartz] - 定时任务模块 已启用，Job 正在运行");
                } else {
                    log.info("[quartz] - 定时任务模块 已启用，暂无运行 Job");
                }
            } else {
                log.info("[quartz] - 定时任务模块 已禁用");
            }

        };
    }

    @Bean
    public SchedulerManager schedulerManager(Optional<Scheduler> scheduler) {
        return scheduler.map(SchedulerManager::new).orElseGet(() -> new SchedulerManager(null));
    }

    @Bean
    @ConditionalOnMissingBean
    public JobLogHandlerService jobLogHandlerService() {
        return new JobLogDefaultHandlerServiceImpl();
    }
}
