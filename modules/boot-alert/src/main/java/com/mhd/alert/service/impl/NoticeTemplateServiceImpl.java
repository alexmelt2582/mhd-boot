package com.mhd.alert.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mhd.alert.config.NoticeTemplateConfig;
import com.mhd.alert.entity.NoticeTemplate;
import com.mhd.alert.mapper.NoticeTemplateMapper;
import com.mhd.alert.service.NoticeTemplateService;
import org.springframework.stereotype.Service;

/**
 * 通知模板表Service实现类
 *
 * @author zhao-hao-dong
 */
@Service
public class NoticeTemplateServiceImpl extends ServiceImpl<NoticeTemplateMapper, NoticeTemplate>
        implements NoticeTemplateService {

    @Override
    public NoticeTemplate selectById(Long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public NoticeTemplate getDefaultNoticeTemplateByType(Integer type) {
        if (type == null) return null;
        return NoticeTemplateConfig.getPRESET_TEMPLATE().get(type);
    }
}




