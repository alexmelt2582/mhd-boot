package com.mhd.alert.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mhd.alert.entity.AlertSingle;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mhd.boot.common.mybatis.core.wrapper.LambdaQueryWrapperX;

/**
 * 单条告警记录表Mapper接口
 *
 * @author zhao-hao-dong
 */
public interface AlertSingleMapper extends BaseMapper<AlertSingle> {

    default AlertSingle selectByFingerprint(String fingerprint) {
        LambdaQueryWrapperX<AlertSingle> queryWrapper = new LambdaQueryWrapperX<>();
        queryWrapper.eq(AlertSingle::getFingerprint, fingerprint);
        return this.selectOne(queryWrapper);
    }
}




