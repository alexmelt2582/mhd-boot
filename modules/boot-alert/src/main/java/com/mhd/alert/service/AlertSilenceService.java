package com.mhd.alert.service;

import com.mhd.alert.entity.AlertSilence;

import java.util.List;

/**
 * 告警静默策略表Service接口
 *
 * @author zhao-hao-dong
 */
public interface AlertSilenceService {

    /**
     * 查询所有已启用的告警静默规则。
     *
     * <p>供 {@code AlarmSilenceReduce} 在缓存未命中时回源加载，以及规则变更后刷新缓存使用。
     *
     * @return 已启用（{@code enable=1}）的静默规则列表，无则返回空列表
     */
    List<AlertSilence> findAlertSilencesByEnableTrue();

    /**
     * 新增或更新静默规则。
     *
     * <p>供 {@code AlarmSilenceReduce} 在命中静默时段后累计 {@code times} 计数并落库使用，
     * 实现层委托 {@code ServiceImpl.saveOrUpdate}。
     *
     * @param alertSilence 待持久化的静默规则
     * @return true 表示新增或更新成功
     */
    boolean saveOrUpdate(AlertSilence alertSilence);
}
