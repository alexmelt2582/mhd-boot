package com.mhd.boot.common.security.core;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author zhao-hao-dong
 */
@Data
public class LoginUser {
    /**
     * 用户编号
     */
    private Long id;
    /**
     * 用户名称
     */
    private String username;
    /**
     * 用户类型
     */
    private Integer userType;
    /**
     * 用户信息
     */
    private Map<String, String> userInfo;
    /**
     * 授权范围
     */
    private List<String> scopes;
}
