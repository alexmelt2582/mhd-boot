package com.mhd.alert.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mhd.alert.entity.AlertSilence;
import com.mhd.alert.mapper.AlertSilenceMapper;
import com.mhd.alert.service.AlertSilenceService;
import com.mhd.boot.common.mybatis.core.wrapper.LambdaQueryWrapperX;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 告警静默策略表Service实现类
 *
 * @author zhao-hao-dong
 */
@Service
public class AlertSilenceServiceImpl extends ServiceImpl<AlertSilenceMapper, AlertSilence>
        implements AlertSilenceService {

    /**
     * 查询所有已启用的告警静默规则。
     *
     * @return 已启用（{@code enable=1}）的静默规则列表
     */
    @Override
    public List<AlertSilence> findAlertSilencesByEnableTrue() {
        LambdaQueryWrapperX<AlertSilence> queryWrapper = new LambdaQueryWrapperX<>();
        queryWrapper.eq(AlertSilence::getEnable, 1);
        return list(queryWrapper);
    }

    /**
     * 新增或更新静默规则，委托 {@code ServiceImpl.saveOrUpdate} 完成实际持久化。
     *
     * @param alertSilence 待持久化的静默规则
     * @return true 表示新增或更新成功
     */
    @Override
    public boolean saveOrUpdate(AlertSilence alertSilence) {
        return super.saveOrUpdate(alertSilence);
    }
}




