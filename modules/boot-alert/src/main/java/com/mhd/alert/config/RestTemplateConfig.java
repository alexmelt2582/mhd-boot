package com.mhd.alert.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * HTTP 客户端配置。
 *
 * <p>Spring Boot 不会自动注册 {@link RestTemplate}（与 {@link RestTemplateBuilder} 不同），
 * 而 {@code AbstractAlertNoticeHandlerImpl} 通过 @Autowired 注入它，所有告警通知 handler
 * （Email / Sms / Webhook / 钉钉 / 企业微信等）均依赖该实例发送回调请求。
 * 故需在此显式声明一个 {@link RestTemplate} Bean。
 *
 * @author zhao-hao-dong
 */
@Configuration
public class RestTemplateConfig {

    /**
     * 构建默认的 {@link RestTemplate}。
     *
     * <p>使用 Spring Boot 自动配置的 {@link RestTemplateBuilder} 构建，
     * 以获得标准的消息转换器与默认超时配置。
     *
     * @param builder Spring Boot 自动注入的 RestTemplateBuilder
     * @return 已完成基础配置的 RestTemplate 实例
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
}
