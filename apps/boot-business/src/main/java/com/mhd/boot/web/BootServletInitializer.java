package com.mhd.boot.web;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * 继承 SpringBootServletInitializer，让外部容器能识别并启动 Spring Boot
 * 用于部署 war 包
 *
 * @author zhao-hao-dong
 **/
public class BootServletInitializer extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        // 告诉 Spring Boot：主配置类
        return application.sources(BootApplication.class);
    }
}
