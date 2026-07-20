package com.mhd.boot.common.mybatis.core.wrapper;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;

/**
 * 拓展 MyBatis Plus LambdaUpdateWrapper 类，主要增加如下功能：
 * <p>
 * 拼接条件的方法，增加 xxxIfPresent 方法，用于判断值不存在的时候，不要拼接到条件中。
 *
 * @author zhao-hao-dong
 **/
public class LambdaUpdateWrapperX<T> extends LambdaUpdateWrapper<T> {
    public LambdaUpdateWrapperX<T> eqIfPresent(SFunction<T, ?> column, Object val) {
        if (ObjectUtil.isNotEmpty(val)) {
            return (LambdaUpdateWrapperX<T>) super.eq(column, val);
        }
        return this;
    }

    public LambdaUpdateWrapperX<T> setIfPresent(SFunction<T, ?> column, Object val) {
        if (ObjectUtil.isNotEmpty(val)) {
            return (LambdaUpdateWrapperX<T>) super.set(column, val);
        }
        return this;
    }

    @Override
    public LambdaUpdateWrapperX<T> eq(boolean condition, SFunction<T, ?> column, Object val) {
        super.eq(condition, column, val);
        return this;
    }

    @Override
    public LambdaUpdateWrapperX<T> eq(SFunction<T, ?> column, Object val) {
        super.eq(column, val);
        return this;
    }

    @Override
    public LambdaUpdateWrapperX<T> set(boolean condition, SFunction<T, ?> column, Object val, String mapping) {
        super.set(condition, column, val, mapping);
        return this;
    }

    @Override
    public LambdaUpdateWrapperX<T> set(SFunction<T, ?> column, Object val) {
        super.set(column, val);
        return this;
    }
}
