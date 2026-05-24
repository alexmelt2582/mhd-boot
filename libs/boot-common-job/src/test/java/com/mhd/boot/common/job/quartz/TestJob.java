package com.mhd.boot.common.job.quartz;

import com.mhd.boot.common.job.quartz.core.handler.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author zhao-hao-dong
 **/
@Component
@Slf4j
public class TestJob implements JobHandler {
    @Override
    public String execute(String params) throws Exception {
        log.info("TestJob");
        return "";
    }
}
