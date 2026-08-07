package com.mhd.alert.service;

import com.mhd.alert.entity.AlertGroup;
import com.mhd.alert.entity.NoticeRule;

import java.util.List;

/**
 * 通知策略表Service接口
 *
 * @author zhao-hao-dong
 */
public interface NoticeRuleService {
    NoticeRule getById(Long id);

    List<NoticeRule> getReceiverFilterRule(AlertGroup alert);

    List<NoticeRule> findNoticeRulesByEnableTrue();
}
