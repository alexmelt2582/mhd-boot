package com.mhd.alert.notice;

import com.mhd.alert.entity.AlertGroup;
import com.mhd.alert.entity.NoticeReceiver;
import com.mhd.alert.entity.NoticeTemplate;

/**
 * @author zhao-hao-dong
 */
public interface AlertNoticeHandler {
    /**
     * 发送告警通知
     */
    void send(NoticeReceiver receiver, NoticeTemplate noticeTemplate, AlertGroup alert) throws AlertNoticeException;

    /**
     * 通知类型
     */
    AlertNoticeTypeEnum type();
}
