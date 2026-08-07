package com.mhd.alert.service;

import com.mhd.alert.entity.NoticeReceiver;

/**
 * 通知接收人表Service接口
 *
 * @author zhao-hao-dong
 */
public interface NoticeReceiverService {
    NoticeReceiver selectById(Long id);
    boolean sendTestMsg(NoticeReceiver noticeReceiver);
}
