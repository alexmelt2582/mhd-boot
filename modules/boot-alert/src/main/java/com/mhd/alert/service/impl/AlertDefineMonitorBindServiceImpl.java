package com.mhd.alert.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mhd.alert.entity.AlertDefineMonitorBind;
import com.mhd.alert.mapper.AlertDefineMonitorBindMapper;
import com.mhd.alert.service.AlertDefineMonitorBindService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 告警规则与监控项绑定关系表Service实现类
 *
 * @author zhao-hao-dong
 */
@Service
@Slf4j
public class AlertDefineMonitorBindServiceImpl extends ServiceImpl<AlertDefineMonitorBindMapper, AlertDefineMonitorBind>
        implements AlertDefineMonitorBindService {

}




