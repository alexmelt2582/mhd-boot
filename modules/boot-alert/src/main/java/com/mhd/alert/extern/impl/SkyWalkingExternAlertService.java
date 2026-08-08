package com.mhd.alert.extern.impl;

import com.mhd.alert.entity.AlertEvent;
import com.mhd.alert.enums.AlertStatusEnum;
import com.mhd.alert.enums.ExtrenAlertEnum;
import com.mhd.alert.extern.ExternAlertService;
import com.mhd.alert.extern.dto.SkyWalkingExternAlert;
import com.mhd.alert.reduce.AlarmCommonReduce;
import com.mhd.boot.common.utils.json.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SkyWalking external alarm service impl
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SkyWalkingExternAlertService implements ExternAlertService {

    private final AlarmCommonReduce alarmCommonReduce;

    @Override
    public void addExternAlert(String content) {
        TypeReference<List<SkyWalkingExternAlert>> typeReference = new TypeReference<>() {};
        List<SkyWalkingExternAlert> alerts = JsonUtils.parseObject(content, typeReference);
        if (alerts == null || alerts.isEmpty()) {
            log.warn("Parse SkyWalking extern alert content failed! content: {}", content);
            return;
        }
        for (SkyWalkingExternAlert alert : alerts) {
            AlertEvent singleAlert = AlertEvent.builder()
                    .content(alert.getAlarmMessage())
                    .status(AlertStatusEnum.FIRING.getCode())
                    .activeAt(Instant.now().toEpochMilli())
                    .startAt(alert.getStartTime() != null ? alert.getStartTime() : Instant.now().toEpochMilli())
                    .labels(acquireAlertLabels(alert))
                    .annotations(acquireAlertAnnotations(alert))
                    .triggerTimes(1)
                    .build();
            alarmCommonReduce.reduceAndSendAlarm(singleAlert);
        }
    }

    @Override
    public String supportSource() {
        return ExtrenAlertEnum.SKYWALKING.getCode();
    }

    private Map<String, String> acquireAlertLabels(SkyWalkingExternAlert externAlert){
        Map<String, String> labels = new HashMap<>(8);
        labels.put("__source__", "skywalking");
        List<SkyWalkingExternAlert.Tag> tags = externAlert.getTags();
        if (tags == null || tags.isEmpty()){
            return labels;
        }
        tags.forEach(tag -> labels.put(tag.getKey(), tag.getValue()));
        return labels;
    }

    private Map<String, String> acquireAlertAnnotations(SkyWalkingExternAlert externAlert){
        Map<String, String> annotations = new HashMap<>(8);
        annotations.putIfAbsent("scope", externAlert.getScope());
        annotations.putIfAbsent("name", externAlert.getName());
        annotations.putIfAbsent("id0", externAlert.getId0());
        annotations.putIfAbsent("id1", externAlert.getId1());
        annotations.putIfAbsent("ruleName", externAlert.getRuleName());
        return annotations;
    }

}
