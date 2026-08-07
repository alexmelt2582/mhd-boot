package com.mhd.alert.service;

import com.mhd.alert.entity.AlertDefine;

import java.util.List;

/**
 * 告警规则定义表Service接口
 *
 * @author zhao-hao-dong
 */
public interface AlertDefineService {

    List<AlertDefine> selectListByTypeAndEnableTrue(String type);
}
