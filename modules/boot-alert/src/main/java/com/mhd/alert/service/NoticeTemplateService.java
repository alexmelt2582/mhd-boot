package com.mhd.alert.service;

import com.mhd.alert.entity.NoticeTemplate;

/**
 * 通知模板表Service接口
 *
 * @author zhao-hao-dong
 */
public interface NoticeTemplateService {
    NoticeTemplate selectById(Long id);
    NoticeTemplate getDefaultNoticeTemplateByType(Integer type);
}
