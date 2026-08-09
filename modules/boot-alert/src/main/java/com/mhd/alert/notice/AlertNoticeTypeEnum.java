package com.mhd.alert.notice;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 告警通知渠道类型枚举。
 *
 * <p>枚举值与 {@code NoticeReceiver.type} 字段一一对应，用于 {@link AlertNoticeHandler#type()}
 * 标识处理器所属渠道，并在 {@code AlertNoticeDispatch} 中按 {@link #getCode()} 路由通知。
 *
 * <p>渠道编号约定（与 {@code NoticeReceiver.type} 注释保持一致）：
 * <ul>
 *   <li>{@link #SMS}(0) 短信</li>
 *   <li>{@link #EMAIL}(1) 邮件</li>
 *   <li>{@link #WEB_HOOK}(2) Webhook</li>
 *   <li>{@link #WE_CHAT}(3) 微信公众号</li>
 *   <li>{@link #WE_COM_ROBOT}(4) 企微机器人</li>
 *   <li>{@link #DING_TALK_ROBOT}(5) 钉钉机器人</li>
 *   <li>{@link #FLY_BOOK_ROBOT}(6) 飞书机器人</li>
 *   <li>{@link #TELEGRAM}(7) Telegram</li>
 *   <li>{@link #SLACK}(8) Slack</li>
 *   <li>{@link #DISCORD}(9) Discord</li>
 *   <li>{@link #WE_COM_APP}(10) 企微应用消息</li>
 *   <li>{@link #HUAWEI_SMN}(11) 华为云 SMN</li>
 *   <li>{@link #SERVER_CHAN}(12) Server 酱</li>
 *   <li>{@link #GOTIFY}(13) Gotify</li>
 *   <li>{@link #FEI_SHU_APP}(14) 飞书应用消息</li>
 *   <li>{@link #NTFY}(15) Ntfy</li>
 * </ul>
 *
 * @author zhao-hao-dong
 */
@AllArgsConstructor
@Getter
@ToString
public enum AlertNoticeTypeEnum {

    /**
     * 短信
     */
    SMS(0, "sms"),

    /**
     * 邮件
     */
    EMAIL(1, "email"),

    /**
     * Webhook
     */
    WEB_HOOK(2, "webhook"),

    /**
     * 微信公众号
     */
    WE_CHAT(3, "wechat"),

    /**
     * 企微机器人
     */
    WE_COM_ROBOT(4, "wecomRobot"),

    /**
     * 钉钉机器人
     */
    DING_TALK_ROBOT(5, "dingTalkRobot"),

    /**
     * 飞书机器人
     */
    FLY_BOOK_ROBOT(6, "flyBookRobot"),

    /**
     * Telegram
     */
    TELEGRAM(7, "telegram"),

    /**
     * Slack
     */
    SLACK(8, "slack"),

    /**
     * Discord
     */
    DISCORD(9, "discord"),

    /**
     * 企微应用消息
     */
    WE_COM_APP(10, "wecomApp"),

    /**
     * 华为云 SMN
     */
    HUAWEI_SMN(11, "huaweiSmn"),

    /**
     * Server 酱
     */
    SERVER_CHAN(12, "serverChan"),

    /**
     * Gotify
     */
    GOTIFY(13, "gotify"),

    /**
     * 飞书应用消息
     */
    FEI_SHU_APP(14, "feiShuApp"),

    /**
     * Ntfy
     */
    NTFY(15, "ntfy");

    /**
     * 渠道编号，对应 {@code NoticeReceiver.type}
     */
    private final int code;

    /**
     * 渠道描述，用于日志与异常信息展示
     */
    private final String description;
}
