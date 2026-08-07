package com.mhd.alert.service.impl;

import com.mhd.alert.entity.AlertDefine;
import com.mhd.alert.enums.EnableEnum;
import com.mhd.alert.mapper.AlertDefineMapper;
import com.mhd.alert.service.AlertDefineService;
import com.mhd.boot.common.mybatis.core.wrapper.LambdaQueryWrapperX;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 告警规则定义表Service实现类
 *
 * @author zhao-hao-dong
 */
@Service
@RequiredArgsConstructor
public class AlertDefineServiceImpl implements AlertDefineService {
    private final AlertDefineMapper baseMapper;

    @Override
    public List<AlertDefine> selectListByTypeAndEnableTrue(String type) {
        LambdaQueryWrapperX<AlertDefine> queryWrapper = new LambdaQueryWrapperX<>();
        queryWrapper.eq(AlertDefine::getType, type);
        queryWrapper.eq(AlertDefine::getEnable, EnableEnum.ENABLE.getCode());
        return baseMapper.selectList(queryWrapper);
    }
}




