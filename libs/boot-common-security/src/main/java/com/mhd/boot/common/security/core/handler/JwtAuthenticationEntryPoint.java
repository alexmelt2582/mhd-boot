package com.mhd.boot.common.security.core.handler;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 访问一个需要认证的 URL 资源，但是此时自己尚未认证（登录）的情况下，返回 401 Unauthorized 错误。
 *
 * @author zhao-hao-dong
 */
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        log.warn("[security][Unauthorized access to URL({})] Exception: {}",
                request.getRequestURI(), authException.getMessage());
        Map<String, Object> result = new HashMap<>(3);
        result.put("code", HttpStatus.UNAUTHORIZED.value());
        result.put("message", "登录状态已过期，请重新登录");
        result.put("data", null);
        String jsonResponse = JSON.toJSONString(result);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        objectMapper.writeValue(response.getWriter(), jsonResponse);
    }
}
