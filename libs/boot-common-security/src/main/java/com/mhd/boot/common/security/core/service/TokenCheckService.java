package com.mhd.boot.common.security.core.service;

import com.mhd.boot.common.security.core.LoginUser;

/**
 * @author zhao-hao-dong
 */
public interface TokenCheckService {
    /**
     * 检查token是否有效
     */
    boolean checkToken(String token);

    /**
     * 获取登录用户信息
     */
    LoginUser getLoginUser(String token);
}
