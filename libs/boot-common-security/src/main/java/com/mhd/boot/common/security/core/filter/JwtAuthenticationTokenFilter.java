package com.mhd.boot.common.security.core.filter;

import com.mhd.boot.common.security.core.service.TokenCheckService;
import com.mhd.boot.common.security.core.util.SecurityFrameworkUtils;
import com.mhd.boot.common.security.config.SecurityProperties;
import com.mhd.boot.common.security.core.LoginUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

/**
 * JWT Token认证过滤器
 *
 * @author zhao-hao-dong
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    private final SecurityProperties securityProperties;
    private final TokenCheckService tokenCheckService;

    @Override
    @SuppressWarnings("NullableProblems")
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = SecurityFrameworkUtils.obtainAuthorization(request, securityProperties.getTokenHeader(), null, securityProperties.getTokenStartWith());
        if (Objects.isNull(token) || token.isBlank()) {
            if (tokenCheckService == null) {
                log.warn("TokenCheckService is not set, skipping token validation.");
            } else {
                try {
                    // 检查Token是否有效
                    LoginUser loginUser = null;
                    try {
                        if (tokenCheckService.checkToken(token)) {
                            loginUser = tokenCheckService.getLoginUser(token);
                        } else {
                            log.warn("Invalid token: {}", token);
                        }
                    } catch (Exception ex) {
                        log.error("Error while checking token: {}", token, ex);
                    }
                    if (loginUser != null) {
                        SecurityFrameworkUtils.setLoginUser(loginUser, request);
                    }
                } catch (Throwable ex) {
                    log.error("Error setting login user from token: {}", token, ex);
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
