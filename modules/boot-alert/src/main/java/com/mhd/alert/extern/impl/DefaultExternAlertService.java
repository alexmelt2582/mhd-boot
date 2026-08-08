package com.mhd.alert.extern.impl;

import com.mhd.alert.entity.AlertEvent;
import com.mhd.alert.enums.AlertStatusEnum;
import com.mhd.alert.enums.ExtrenAlertEnum;
import com.mhd.alert.extern.ExternAlertService;
import com.mhd.alert.reduce.AlarmCommonReduce;
import com.mhd.boot.common.utils.json.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 *
 *
 * @author zhao-hao-dong
 **/
@RequiredArgsConstructor
@Slf4j
@Service
public class DefaultExternAlertService implements ExternAlertService {
    private final AlarmCommonReduce alarmCommonReduce;

    @Override
    public void addExternAlert(String content) {
        AlertEvent alert = JsonUtils.parseObject(content, AlertEvent.class);
        if (alert == null) {
            log.warn("parse extern alert content failed! content: {}", content);
            throw new IllegalArgumentException("parse extern alert content failed!");
        }
        alert.setId(null);
        String status = alert.getStatus();
        if (status == null) {
            if (alert.getStartAt() != null && alert.getEndAt() != null) {
                if (alert.getEndAt() < Instant.now().toEpochMilli()) {
                    status = AlertStatusEnum.RESOLVED.getCode();
                } else {
                    status = AlertStatusEnum.FIRING.getCode();
                }
            } else {
                status = AlertStatusEnum.FIRING.getCode();
            }
        }
        alert.setStatus(status);
        if (AlertStatusEnum.FIRING.getCode().equals(status)) {
            alert.setEndAt(null);
            if (alert.getStartAt() == null) {
                alert.setStartAt(Instant.now().toEpochMilli());
            }
            if (alert.getActiveAt() == null) {
                alert.setActiveAt(Instant.now().toEpochMilli());
            }
        } else {
            alert.setActiveAt(null);
            if (alert.getStartAt() == null) {
                alert.setStartAt(Instant.now().toEpochMilli());
            }
            if (alert.getEndAt() == null) {
                alert.setEndAt(Instant.now().toEpochMilli());
            }
        }
        alarmCommonReduce.reduceAndSendAlarm(alert);
    }

    @Override
    public String supportSource() {
        return ExtrenAlertEnum.DEFAULT.getSource();
    }
}
