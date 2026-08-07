package com.mhd.alert.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mhd.boot.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知接收人表
 */
@TableName(value = "hzb_notice_receiver")
@Data
@EqualsAndHashCode(callSuper = true)
public class NoticeReceiver extends BaseEntity {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 接收人名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 通知方式: 0-SMS 1-Email 2-Webhook 3-微信公众号 4-企微机器人 5-钉钉机器人 6-飞书机器人 7-Telegram 8-Slack 9-Discord 10-企微应用消息 11-华为SMN 12-Server酱 13-Gotify 14-飞书应用消息 15-Ntfy
     */
    @TableField(value = "type")
    private Integer type;

    /**
     * 手机号(SMS时有效)
     */
    @TableField(value = "phone")
    private String phone;

    /**
     * 邮箱(Email时有效)
     */
    @TableField(value = "email")
    private String email;

    /**
     * Webhook URL
     */
    @TableField(value = "hook_url")
    private String hookUrl;

    /**
     * Webhook认证类型: None / Basic / Bearer
     */
    @TableField(value = "hook_auth_type")
    private String hookAuthType;

    /**
     * Webhook认证Token
     */
    @TableField(value = "hook_auth_token")
    private String hookAuthToken;

    /**
     * OpenID(微信公众号/企微/飞书机器人)
     */
    @TableField(value = "wechat_id")
    private String wechatId;

    /**
     * 飞书应用App ID
     */
    @TableField(value = "app_id")
    private String appId;

    /**
     * Access Token(钉钉机器人)
     */
    @TableField(value = "access_token")
    private String accessToken;

    /**
     * Telegram Bot Token
     */
    @TableField(value = "tg_bot_token")
    private String tgBotToken;

    /**
     * Telegram User ID
     */
    @TableField(value = "tg_user_id")
    private String tgUserId;

    /**
     * Telegram Message Thread ID
     */
    @TableField(value = "tg_message_thread_id")
    private String tgMessageThreadId;

    /**
     * 飞书应用消息接收类型: 0-user 1-chat 2-party 3-all
     */
    @TableField(value = "lark_receive_type")
    private Integer larkReceiveType;

    /**
     * 钉钉/飞书/企微用户ID
     */
    @TableField(value = "user_id")
    private String userId;

    /**
     * 飞书应用消息chatId
     */
    @TableField(value = "chat_id")
    private String chatId;

    /**
     * Slack Webhook URL
     */
    @TableField(value = "slack_web_hook_url")
    private String slackWebHookUrl;

    /**
     * 企微企业ID
     */
    @TableField(value = "corp_id")
    private String corpId;

    /**
     * 企微应用AgentId
     */
    @TableField(value = "agent_id")
    private Integer agentId;

    /**
     * 企微应用Secret
     */
    @TableField(value = "app_secret")
    private String appSecret;

    /**
     * 企微部门ID
     */
    @TableField(value = "party_id")
    private String partyId;

    /**
     * 企微标签ID
     */
    @TableField(value = "tag_id")
    private String tagId;

    /**
     * Discord频道ID
     */
    @TableField(value = "discord_channel_id")
    private String discordChannelId;

    /**
     * Discord Bot Token
     */
    @TableField(value = "discord_bot_token")
    private String discordBotToken;

    /**
     * 华为云SMN AccessKey
     */
    @TableField(value = "smn_ak")
    private String smnAk;

    /**
     * 华为云SMN SecretKey
     */
    @TableField(value = "smn_sk")
    private String smnSk;

    /**
     * 华为云SMN项目ID
     */
    @TableField(value = "smn_project_id")
    private String smnProjectId;

    /**
     * 华为云SMN区域
     */
    @TableField(value = "smn_region")
    private String smnRegion;

    /**
     * 华为云SMN TopicUrn
     */
    @TableField(value = "smn_topic_urn")
    private String smnTopicUrn;

    /**
     * Server酱Token
     */
    @TableField(value = "server_chan_token")
    private String serverChanToken;

    /**
     * Gotify Token
     */
    @TableField(value = "gotify_token")
    private String gotifyToken;

    /**
     * Ntfy服务器URL
     */
    @TableField(value = "ntfy_server_url")
    private String ntfyServerUrl;

    /**
     * Ntfy主题
     */
    @TableField(value = "ntfy_topic")
    private String ntfyTopic;

    /**
     * Ntfy访问Token
     */
    @TableField(value = "ntfy_token")
    private String ntfyToken;
}