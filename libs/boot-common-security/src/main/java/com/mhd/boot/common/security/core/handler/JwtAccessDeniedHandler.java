package com.mhd.boot.common.security.core.handler;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mhd.boot.common.security.core.util.SecurityFrameworkUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import com.mhd.boot.common.security.core.LoginUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 访问一个需要认证的 URL 资源，已经认证（登录）但是没有权限的情况下，返回 403 Forbidden 错误。
 *
 * @author zhao-hao-dong
 */
@Slf4j
public class JwtAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        Long userId = loginUser != null ? loginUser.getId() : null;
        String username = loginUser != null ? loginUser.getUsername() : null;
        log.warn("[security][Access denied for URL({}) by user(id: {}, name: {})] Exception: {}",
                request.getRequestURI(), userId, username, accessDeniedException.getMessage());
        Map<String, Object> result = new HashMap<>(3);
        result.put("code", HttpStatus.FORBIDDEN.value());
        result.put("message", "禁止访问，您没有权限访问此资源");
        result.put("data", null);
        String jsonResponse = JSON.toJSONString(result);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
        response.setStatus(HttpStatus.FORBIDDEN.value());
        objectMapper.writeValue(response.getWriter(), jsonResponse);
    }
}
