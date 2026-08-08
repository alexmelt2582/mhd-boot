package com.mhd.alert.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.mhd.boot.common.mybatis.core.domain.BaseEntity;
import com.mhd.boot.common.utils.json.JsonUtils;
import lombok.*;

import java.util.Map;

/**
 * <p>
 * 单条告警记录表
 * </p>
 *
 * @author zhao-hao-dong
 * @since 2026-08-08
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("hzb_alert_event")
public class AlertEvent extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 告警指纹(唯一标识)
     */
    @TableField("fingerprint")
    private String fingerprint;

    /**
     * 标签(JSON), 如 {"alertname":"HighCPUUsage","priority":"critical"}
     */
    @TableField(value = "labels", typeHandler = JacksonTypeHandler.class)
    private Map<String, String> labels;

    /**
     * 注解(JSON), 如 {"summary":"High CPU usage detected"}
     */
    @TableField(value = "annotations", typeHandler = JacksonTypeHandler.class)
    private Map<String, String> annotations;

    /**
     * 告警内容描述
     */
    @TableField("content")
    private String content;

    /**
     * 状态: firing-告警中 / resolved-已恢复
     */
    @TableField("status")
    private String status;

    /**
     * 触发次数
     */
    @TableField("trigger_times")
    private Integer triggerTimes;

    /**
     * 告警开始时间(毫秒时间戳)
     */
    @TableField("start_at")
    private Long startAt;

    /**
     * 告警活跃时间(毫秒时间戳)
     */
    @TableField("active_at")
    private Long activeAt;

    /**
     * 告警结束时间(毫秒时间戳), resolved时才有值
     */
    @TableField("end_at")
    private Long endAt;

    @Override
    public AlertEvent clone() {
        return JsonUtils.parseObject(JsonUtils.toJsonString(this), AlertEvent.class);
    }
}
