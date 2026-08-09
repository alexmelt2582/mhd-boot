package com.mhd.alert.notice.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 机器人通知通用响应体。
 *
 * <p>钉钉、飞书、企微等机器人 webhook 返回的 JSON 结构字段命名不统一（{@code errcode/errmsg}
 * 与 {@code code/msg} 并存），本类将两种常见结构合并到同一 DTO，便于
 * {@code AlertNoticeHandler} 统一反序列化并判定发送是否成功。
 *
 * <p>典型用法：{@code restTemplate.postForObject(url, req, CommonRobotNotifyResp.class)}
 *
 * @author zhao-hao-dong
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommonRobotNotifyResp {

    /**
     * 错误码（钉钉 / 企微机器人使用），0 通常表示成功
     */
    @JsonProperty(value = "errcode")
    private Integer errCode;

    /**
     * 错误信息（钉钉 / 企微机器人使用）
     */
    @JsonProperty(value = "errmsg")
    private String errMsg;

    /**
     * 错误码（飞书等使用），0 通常表示成功
     */
    private Integer code;

    /**
     * 错误信息（飞书等使用）
     */
    private String msg;
}
