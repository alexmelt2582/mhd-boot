package com.mhd.boot.common.security.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;

/**
 * 安全配置属性
 *
 * @author zhao-hao-dong
 */
@ConfigurationProperties(prefix = "mhd.auth.jwt")
@Validated
@Data
public class SecurityProperties {

    /**
     * 访问令牌的请求 Header，默认 "Authorization"
     */
    @NotBlank(message = "Token Header 不能为空")
    private String tokenHeader = "Authorization";

    /**
     * 令牌前缀，默认 "Bearer "
     */
    private String tokenStartWith = "Bearer ";

    /**
     * JWT密钥（Base64编码）88 位
     */
    @NotBlank(message = "JWT密钥不能为空")
    private String secret = "ZmQ0ZGI5NjQ0MDQwY2I4MjMxY2Y3ZmI3MjdhN2ZmMjNhODViOTg1ZGE0NTBjMGM4NDA5NzYxMjdjOWMwYWRmZTBlZjlhNGY3ZTg4Y2U3YTE1ODVkZDU5Y2Y3OGYwZWE1NzUzNWQ2YjFjZDc0NGMxZWU2MmQ3MjY1NzJmNTE0MzI=";

    /**
     * PasswordEncoder 加密复杂度，越高开销越大，默认 4
     */
    private Integer passwordEncoderLength = 4;

    /**
     * Token有效期（秒），默认 7 小时 60 * 60 * 24 * 7
     */
    private Long expiration = 604800L;

    /**
     * 认证策略：
     * WHITE = 仅 authPathPatterns 中的路径需要登录，其余放行
     * BLACK = 仅 excludePathPatterns 中的路径放行，其余需要登录
     */
    private AuthStrategy authStrategy = AuthStrategy.BLACK;

    /**
     * 需要认证的路径（WHITE 模式生效）
     */
    private List<String> authPathPatterns = Collections.emptyList();

    /**
     * 放行的路径（BLACK 模式生效）
     */
    private List<String> excludePathPatterns = Collections.emptyList();

    public enum AuthStrategy { WHITE, BLACK }
}
