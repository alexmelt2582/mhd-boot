package com.mhd.alert.mapper;

import com.mhd.alert.entity.AlertGroup;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mhd.boot.common.mybatis.core.wrapper.LambdaQueryWrapperX;

/**
 * 分组告警记录表Mapper接口
 *
 * @author zhao-hao-dong
 */
public interface AlertGroupMapper extends BaseMapper<AlertGroup> {
    default AlertGroup selectByGroupKey(String groupKey) {
        LambdaQueryWrapperX<AlertGroup> queryWrapper = new LambdaQueryWrapperX<>();
        queryWrapper.eq(AlertGroup::getGroupKey, groupKey);
        return this.selectOne(queryWrapper);
    }
}




