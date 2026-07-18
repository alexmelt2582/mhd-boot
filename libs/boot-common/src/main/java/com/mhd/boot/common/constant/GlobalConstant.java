package com.mhd.boot.common.constant;

/**
 * @author zhao-hao-dong
 */
public interface GlobalConstant {
    /**
     * 排除敏感属性字段
     */
    String[] EXCLUDE_PROPERTIES = { "password", "oldPassword", "newPassword", "confirmPassword" };
}
