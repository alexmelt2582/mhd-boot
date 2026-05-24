package com.mhd.boot.common.security.config;

import com.mhd.boot.common.security.core.filter.JwtAuthenticationTokenFilter;
import com.mhd.boot.common.security.core.handler.JwtAccessDeniedHandler;
import com.mhd.boot.common.security.core.handler.JwtAuthenticationEntryPoint;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


/**
 * @author zhao-hao-dong
 */
@AutoConfiguration(after = SecurityAutoConfiguration.class)
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {
    @Resource
    private SecurityProperties securityProperties;
    @Resource
    private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;
    @Resource
    private JwtAccessDeniedHandler jwtAccessDeniedHandler;
    @Resource
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    /**
     * 配置 URL 的安全配置
     * <p>
     * anyRequest | 匹配所有请求路径
     * access | SpringEl表达式结果为true时可以访问
     * anonymous | 匿名可以访问
     * denyAll | 用户不能访问
     * fullyAuthenticated | 用户完全认证可以访问（非remember-me下自动登录）
     * hasAnyAuthority | 如果有参数，参数表示权限，则其中任何一个权限可以访问
     * hasAnyRole | 如果有参数，参数表示角色，则其中任何一个角色可以访问
     * hasAuthority | 如果有参数，参数表示权限，则其权限可以访问
     * hasIpAddress | 如果有参数，参数表示IP地址，如果用户IP和参数匹配，则可以访问
     * hasRole | 如果有参数，参数表示角色，则其角色可以访问
     * permitAll | 用户可以任意访问
     * rememberMe | 允许通过remember-me登录的用户访问
     * authenticated | 用户登录后可访问
     */
    @Bean
    SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                // 配置授权规则
                .authorizeHttpRequests(auth -> {
                    // OPTIONS 永远放行
                    auth.requestMatchers(HttpMethod.OPTIONS).permitAll();
                    switch (securityProperties.getAuthStrategy()) {
                        case WHITE:
                            // 只有配置的路径才认证
                            auth.requestMatchers(securityProperties.getAuthPathPatterns().toArray(new String[0]))
                                    .authenticated()
                                    .anyRequest().permitAll();
                            break;
                        case BLACK:
                            // 先放行配置的，其余全部认证
                            auth.requestMatchers(securityProperties.getExcludePathPatterns().toArray(new String[0]))
                                    .permitAll()
                                    .anyRequest().authenticated();
                            break;
                    }
                })
                // 跨域配置
                .cors(Customizer.withDefaults())
                // 禁用 CSRF
                .csrf(AbstractHttpConfigurer::disable)
                // 会话管理（无状态：不创建、不使用 Session，JWT 核心配置）
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 自定义异常处理（权限拒绝 + 未认证入口，lambda 风格消除 and() 链式调用）
                .exceptionHandling(ex -> {
                    ex.accessDeniedHandler(jwtAccessDeniedHandler); // 权限不足处理器
                    ex.authenticationEntryPoint(jwtAuthenticationEntryPoint); // 未认证处理器
                })
                // 添加 JWT 自定义过滤器（在 UsernamePasswordAuthenticationFilter 之前执行）
                .addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);
        return httpSecurity.build();
    }
}
