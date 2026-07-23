package com.mhd.boot.common.operatelog.config;

import com.mhd.boot.common.operatelog.core.aspect.OperateLogAspect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * 操作日志自动配置类。
 * <p>
 * 根据配置文件中 mhd.operate-log.enable 决定是否加载当前模块
 * <p>
 * 如果用户未进行配置，则默认加载
 *
 * @author zhao-hao-dong
 **/
@AutoConfiguration
@Slf4j
public class OperateLogAutoConfiguration {

    @Value("${mhd.operate-log.enable:true}")
    private boolean operateLogEnabled;

    @Bean
    public ApplicationRunner operateLogStatusRunner() {
        return args -> {
            if (operateLogEnabled) {
                log.info("[operateLog] - 操作日志模块 已启用");
            } else {
                log.info("[operateLog] - 操作日志模块 已禁用");
            }
        };
    }

    /**
     * 注册操作日志切面
     */
    @Bean
    @ConditionalOnProperty(prefix = "mhd.operate-log", name = "enable", havingValue = "true", matchIfMissing = true)
    public OperateLogAspect operateLogAspect() {
        return new OperateLogAspect();
    }
}
