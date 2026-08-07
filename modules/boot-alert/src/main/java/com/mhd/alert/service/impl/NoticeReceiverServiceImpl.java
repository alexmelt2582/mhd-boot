package com.mhd.alert.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mhd.alert.entity.NoticeReceiver;
import com.mhd.alert.mapper.NoticeReceiverMapper;
import com.mhd.alert.service.NoticeReceiverService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 通知接收人表Service实现类
 *
 * @author zhao-hao-dong
 */
@Service
@RequiredArgsConstructor
public class NoticeReceiverServiceImpl extends ServiceImpl<NoticeReceiverMapper, NoticeReceiver>
        implements NoticeReceiverService {
    //private final AlertNoticeDispatch alertNoticeDispatch;

    @Override
    public NoticeReceiver selectById(Long id) {
        return this.baseMapper.selectById(id);
    }

    @Override
    public boolean sendTestMsg(NoticeReceiver noticeReceiver) {
        //Map<String, String> labels = new HashMap<>(8);
        //labels.put(AlertConstants.LABEL_INSTANCE, "127.0.0.1");
        //labels.put(AlertConstants.LABEL_ALERT_NAME, "CPU Usage Alert");
        //Map<String, String> annotations = new HashMap<>(8);
        //annotations.put("suggest", "Please check the CPU usage of the server");
        //AlertSingle alertSingle1 = AlertSingle.builder()
        //        .labels(labels)
        //        .content("test send msg! \\n This is the test data. It is proved that it can be received successfully")
        //        .startAt(System.currentTimeMillis())
        //        .activeAt(System.currentTimeMillis())
        //        .endAt(System.currentTimeMillis())
        //        .triggerTimes(2)
        //        .annotations(annotations)
        //        .status(AlertStatusEnum.FIRING.getCode())
        //        .build();
        //AlertSingle alertSingle2 = AlertSingle.builder()
        //        .labels(labels)
        //        .content("test send msg! \\n This is the test data. It is proved that it can be received successfully")
        //        .startAt(System.currentTimeMillis())
        //        .activeAt(System.currentTimeMillis())
        //        .endAt(System.currentTimeMillis())
        //        .triggerTimes(2)
        //        .annotations(annotations)
        //        .status(AlertStatusEnum.FIRING.getCode())
        //        .build();
        //AlertGroup alertGroup = AlertGroup.builder()
        //        .commonLabels(Map.of(AlertConstants.LABEL_ALERT_NAME, "CPU Usage Alert"))
        //        .commonAnnotations(annotations)
        //        .alerts(List.of(alertSingle1, alertSingle2))
        //        .status(AlertStatusEnum.FIRING.getCode())
        //        .build();
        //return alertNoticeDispatch.sendNoticeMsg(noticeReceiver, null, alertGroup);
        return true;
    }
}




