package com.mhd.alert.notice.impl.sms;

import com.mhd.alert.entity.AlertGroup;
import com.mhd.alert.entity.NoticeReceiver;
import com.mhd.alert.entity.NoticeTemplate;
import com.mhd.alert.notice.AbstractAlertNoticeHandlerImpl;
import com.mhd.alert.notice.AlertNoticeException;
import com.mhd.alert.notice.AlertNoticeTypeEnum;

/**
 * @author zhao-hao-dong
 */
public class SmsAlertNoticeHandlerImpl extends AbstractAlertNoticeHandlerImpl {
    @Override
    public void send(NoticeReceiver receiver, NoticeTemplate noticeTemplate, AlertGroup alert) throws AlertNoticeException {

    }

    @Override
    public AlertNoticeTypeEnum type() {
        return AlertNoticeTypeEnum.SMS;
    }
}
