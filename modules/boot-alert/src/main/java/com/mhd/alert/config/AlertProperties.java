package com.mhd.alert.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 告警模块配置属性。
 *
 * <p>对应配置前缀 {@code com.mhd.alert}，可在 {@code application.yml} 中通过
 * {@code com.mhd.alert.console-url=...}、{@code com.mhd.alert.inhibit.ttl=...} 等覆盖默认值。
 *
 * <p>本类集中管理两类配置：
 * <ul>
 *   <li>{@link #consoleUrl}：告警控制台地址，注入通知模板用于渲染「查看详情」链接；</li>
 *   <li>各通知渠道的 webhook URL 模板：钉钉 / 企微 / 飞书 / Telegram / Discord / Server 酱 /
 *       Gotify / Ntfy 等。这些 URL 通常固定不变，但允许通过配置覆盖以适配私有化部署。</li>
 * </ul>
 *
 * <p>URL 模板中的 {@code %s} 占位符由各 {@code AlertNoticeHandler} 在发送时用
 * {@code NoticeReceiver} 中对应的 token / id 替换。
 *
 * @author zhao-hao-dong
 */
@Data
@Component
@ConfigurationProperties(prefix = "com.mhd.alert")
public class AlertProperties {

    /**
     * 告警控制台地址，用于通知模板中生成可点击的告警详情链接，缺省为空。
     */
    private String consoleUrl;

    /**
     * 企微机器人 webhook URL 前缀，拼接 {@code NoticeReceiver.wechatId} 作为完整地址。
     */
    private String weWorkWebhookUrl = "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=";

    /**
     * 钉钉机器人 webhook URL 前缀，拼接 {@code NoticeReceiver.accessToken} 作为完整地址。
     */
    private String dingTalkWebhookUrl = "https://oapi.dingtalk.com/robot/send?access_token=";

    /**
     * 飞书机器人 webhook URL 前缀，拼接 {@code NoticeReceiver.accessToken} 作为完整地址。
     */
    private String flyBookWebhookUrl = "https://open.feishu.cn/open-apis/bot/v2/hook/";

    /**
     * Telegram Bot 发送消息 API URL 模板，{@code %s} 占位符替换为 bot token。
     */
    private String telegramWebhookUrl = "https://api.telegram.org/bot%s/sendMessage";

    /**
     * Discord 频道消息 API URL 模板，{@code %s} 占位符替换为频道 ID。
     */
    private String discordWebhookUrl = "https://discord.com/api/v9/channels/%s/messages";

    /**
     * Server 酱推送 URL 模板，{@code %s} 占位符替换为 Server 酱 token。
     */
    private String serverChanWebhookUrl = "https://sctapi.ftqq.com/%s.send";

    /**
     * Gotify 推送 URL 前缀，拼接 {@code NoticeReceiver.gotifyToken} 作为完整地址。
     */
    private String gotifyWebhookUrl = "https://push.example.de/message?token=";

    /**
     * Ntfy 默认服务器 URL，当 {@code NoticeReceiver.ntfyServerUrl} 为空时回退使用。
     */
    private String ntfyDefaultServerUrl = "https://ntfy.sh";

    /**
     * 抑制配置
     */
    private InhibitProperties inhibit;

    /**
     * 抑制配置
     */
    @Getter
    @Setter
    public static class InhibitProperties {

        /**
         * 抑制规则的过期时间，单位毫秒，默认4小时
         */
        private long ttl = 4 * 60 * 60 * 1000L;
    }
}
