package com.mhd.alert.extern.impl;

import com.mhd.alert.entity.AlertEvent;
import com.mhd.alert.enums.ExtrenAlertEnum;
import com.mhd.alert.extern.ExternAlertService;
import com.mhd.alert.reduce.AlarmCommonReduce;
import com.mhd.boot.common.utils.json.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * zabbix external alarm service impl
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ZabbixExternAlertServiceImpl implements ExternAlertService {

    private final AlarmCommonReduce alarmCommonReduce;

    @Override
    public void addExternAlert(String content) {
        AlertEvent alert = JsonUtils.parseObject(content, AlertEvent.class);
        if (alert == null) {
            log.warn("parse extern alert content failed! content: {}", content);
            return;
        }
        alert.setId(null);
        alarmCommonReduce.reduceAndSendAlarm(alert);
    }

    @Override
    public String supportSource() {
        return ExtrenAlertEnum.VOLCENGINE.getCode();
    }
}
