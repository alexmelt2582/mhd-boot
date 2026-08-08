package com.mhd.boot.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 应用启动入口
 */
@SpringBootApplication(scanBasePackages = {"com.mhd"})
@Slf4j
public class BootApplication {

	/**
	 * 启动应用
	 *
	 * @param args 启动参数
	 */
	public static void main(String[] args) {
		SpringApplication.run(BootApplication.class, args);
		log.info("boot-business started successfully.");
	}
}
