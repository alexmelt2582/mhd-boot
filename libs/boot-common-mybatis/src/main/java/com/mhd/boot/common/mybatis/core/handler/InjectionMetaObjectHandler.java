package com.mhd.boot.common.mybatis.core.handler;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpStatus;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.mhd.boot.common.exception.BusinessException;
import com.mhd.boot.common.mybatis.core.domain.BaseEntity;
import com.mhd.boot.common.security.core.LoginUser;
import com.mhd.boot.common.security.core.util.SecurityFrameworkUtils;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 字段自动填充处理器
 * <p>
 * 在 INSERT 时自动填充 {@code createBy}、{@code createTime}、{@code updateBy} 和 {@code updateTime}。
 * 在 UPDATE 时自动填充 {@code updateBy} 和 {@code updateTime}。
 * 实体类字段须标注 {@code @TableField(fill = FieldFill.INSERT)} 或
 * {@code @TableField(fill = FieldFill.INSERT_UPDATE)}。
 *
 * @author zhao-hao-dong
 */
public class InjectionMetaObjectHandler implements MetaObjectHandler {
    /**
     * 如果用户不存在默认注入-1代表无用户
     */
    private static final Long DEFAULT_USER_ID = -1L;

    /**
     * 插入填充方法，用于在插入数据时自动填充实体对象中的创建时间、更新时间、创建人、更新人等信息
     *
     * @param metaObject 元对象，用于获取原始对象并进行填充
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        try {
            if (ObjectUtil.isNotNull(metaObject) && metaObject.getOriginalObject() instanceof BaseEntity baseEntity) {
                LocalDateTime now = LocalDateTime.now();
                baseEntity.setCreateTime(now);
                baseEntity.setUpdateTime(now);

                // 如果创建人为空，则填充当前登录用户的信息
                if (ObjectUtil.isNull(baseEntity.getCreateBy())) {
                    LoginUser loginUser = getLoginUser();
                    if (ObjectUtil.isNotNull(loginUser)) {
                        Long userId = loginUser.getId();
                        // 填充创建人、更新人和创建部门信息
                        baseEntity.setCreateBy(userId);
                        baseEntity.setUpdateBy(userId);
                    } else {
                        // 填充创建人、更新人和创建部门信息
                        baseEntity.setCreateBy(DEFAULT_USER_ID);
                        baseEntity.setUpdateBy(DEFAULT_USER_ID);
                    }
                }
            } else {
                LocalDateTime now = LocalDateTime.now();
                this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
                this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
            }
        } catch (Exception e) {
            throw new BusinessException(String.valueOf(HttpStatus.HTTP_UNAUTHORIZED), "自动注入异常 => " + e.getMessage());
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        try {
            if (ObjectUtil.isNotNull(metaObject) && metaObject.getOriginalObject() instanceof BaseEntity baseEntity) {
                LocalDateTime now = LocalDateTime.now();
                baseEntity.setUpdateTime(now);

                // 获取当前登录用户的ID，并填充更新人信息
                Long userId = SecurityFrameworkUtils.getLoginUserId();
                if (ObjectUtil.isNotNull(userId)) {
                    baseEntity.setUpdateBy(userId);
                } else {
                    baseEntity.setUpdateBy(DEFAULT_USER_ID);
                }
            } else {
                LocalDateTime now = LocalDateTime.now();
                this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, now);
            }
        } catch (Exception e) {
            throw new BusinessException(String.valueOf(HttpStatus.HTTP_UNAUTHORIZED), "自动注入异常 => " + e.getMessage());
        }
    }

    /**
     * 获取当前登录用户信息
     *
     * @return 当前登录用户的信息，如果用户未登录则返回 null
     */
    private LoginUser getLoginUser() {
        LoginUser loginUser;
        try {
            loginUser = SecurityFrameworkUtils.getLoginUser();
        } catch (Exception e) {
            return null;
        }
        return loginUser;
    }
}
