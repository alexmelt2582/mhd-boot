package com.mhd.boot.common.security.config;

import com.mhd.boot.common.security.core.context.TransmittableThreadLocalSecurityContextHolderStrategy;
import com.mhd.boot.common.security.core.filter.JwtAuthenticationTokenFilter;
import com.mhd.boot.common.security.core.handler.JwtAccessDeniedHandler;
import com.mhd.boot.common.security.core.handler.JwtAuthenticationEntryPoint;
import com.mhd.boot.common.security.core.service.TokenCheckService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.Nullable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Spring Security 自动配置类，主要用于相关组件的配置
 *
 * @author zhao-hao-dong
 */
@EnableConfigurationProperties(SecurityProperties.class)
@AutoConfiguration
public class SecurityAutoConfiguration {
    @Resource
    private SecurityProperties securityProperties;

    /**
     * Spring Security 加密器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(securityProperties.getPasswordEncoderLength());
    }

    /**
     * 认证失败处理类 Bean
     */
    @Bean
    public JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint() {
        return new JwtAuthenticationEntryPoint();
    }

    /**
     * 权限不够处理器 Bean
     */
    @Bean
    public JwtAccessDeniedHandler jwtAccessDeniedHandler() {
        return new JwtAccessDeniedHandler();
    }

    /**
     * Token 认证过滤器 Bean
     */
    @Bean
    public JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter(@Nullable TokenCheckService tokenCheckService) {
        return new JwtAuthenticationTokenFilter(securityProperties, tokenCheckService);
    }

    /**
     * 声明调用 {@link SecurityContextHolder#setStrategyName(String)} 方法，
     * 设置使用 {@link TransmittableThreadLocalSecurityContextHolderStrategy} 作为 Security 的上下文策略
     */
    @Bean
    public MethodInvokingFactoryBean securityContextHolderMethodInvokingFactoryBean() {
        MethodInvokingFactoryBean methodInvokingFactoryBean = new MethodInvokingFactoryBean();
        methodInvokingFactoryBean.setTargetClass(SecurityContextHolder.class);
        methodInvokingFactoryBean.setTargetMethod("setStrategyName");
        methodInvokingFactoryBean.setArguments(TransmittableThreadLocalSecurityContextHolderStrategy.class.getName());
        return methodInvokingFactoryBean;
    }
}
