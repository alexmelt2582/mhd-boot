package com.mhd.alert.store;

import com.mhd.alert.entity.AlertGroup;

/**
 * 告警信息持久化
 *
 * @author zhao-hao-dong
 */
public interface AlertStoreHandler {
    /**
     * 持久化告警记录
     */
    AlertGroup store(AlertGroup alertGroup);
}
