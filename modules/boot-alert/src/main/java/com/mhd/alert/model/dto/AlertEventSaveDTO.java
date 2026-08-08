package com.mhd.alert.model.dto;

import com.mhd.alert.entity.AlertEvent;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * 告警事件 - SaveDTO 对象
 *
 * @author zhao-hao-dong
 */
@Data
@AutoMapper(target = AlertEvent.class, reverseConvertGenerate = false)
public class AlertEventSaveDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 主键ID
     */
    private Long id;
    /**
     * 告警指纹(唯一标识)
     */
    private String fingerprint;
    /**
     * 标签(JSON), 如 {"alertname":"HighCPUUsage","priority":"critical"}
     */
    private Map<String, String> labels;
    /**
     * 注解(JSON), 如 {"summary":"High CPU usage detected"}
     */
    private Map<String, String> annotations;
    /**
     * 告警内容描述
     */
    private String content;
    /**
     * 状态: firing-告警中 / resolved-已恢复
     */
    private String status;
    /**
     * 触发次数
     */
    private Integer triggerTimes;
    /**
     * 告警开始时间(毫秒时间戳)
     */
    private Long startAt;
    /**
     * 告警活跃时间(毫秒时间戳)
     */
    private Long activeAt;
    /**
     * 告警结束时间(毫秒时间戳), resolved时才有值
     */
    private Long endAt;
}