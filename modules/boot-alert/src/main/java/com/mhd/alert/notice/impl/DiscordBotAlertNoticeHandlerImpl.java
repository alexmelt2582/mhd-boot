package com.mhd.alert.notice.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mhd.alert.entity.AlertGroup;
import com.mhd.alert.entity.NoticeReceiver;
import com.mhd.alert.entity.NoticeTemplate;
import com.mhd.alert.notice.AbstractAlertNoticeHandlerImpl;
import com.mhd.alert.notice.AlertNoticeException;
import com.mhd.alert.notice.AlertNoticeTypeEnum;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Discord Bot 渠道通知处理器。
 *
 * <p>通过 Discord 频道消息 API 发送 embed 格式告警消息。请求 URL 由
 * {@link com.mhd.alert.config.AlertProperties#getDiscordWebhookUrl()} 模板（含 {@code %s}）
 * 与频道 ID 拼接而成，鉴权使用 {@code Bot <token>} 头。
 *
 * @author zhao-hao-dong
 */
@Component
@Slf4j
public class DiscordBotAlertNoticeHandlerImpl extends AbstractAlertNoticeHandlerImpl {

    /**
     * 发送 Discord Bot 通知。
     *
     * <p>执行流程：
     * <ol>
     *   <li>渲染模板为 embed 描述并组装请求体（含标题前缀）；</li>
     *   <li>构造鉴权头 {@code Authorization: Bot <token>}；</li>
     *   <li>POST 到频道消息 API 并校验响应中是否含消息 ID。</li>
     * </ol>
     *
     * @param receiver       通知接收人，{@code discordChannelId} 为频道 ID，{@code discordBotToken} 为 bot token
     * @param noticeTemplate 通知模板，渲染为 embed 描述
     * @param alert          告警组，作为模板渲染数据来源
     * @throws AlertNoticeException HTTP 调用失败或响应无消息 ID 时抛出
     */
    @Override
    public void send(NoticeReceiver receiver, NoticeTemplate noticeTemplate, AlertGroup alert) throws AlertNoticeException {
        try {
            // 1. 渲染模板并组装 embed 请求体
            DiscordNotifyDTO notifyBody = DiscordNotifyDTO.builder()
                    .embeds(List.of(EmbedDTO.builder()
                            .title("[" + NOTIFY_TITLE + "]")
                            .description(renderContent(noticeTemplate, alert))
                            .build()))
                    .build();
            String url = String.format(alertProperties.getDiscordWebhookUrl(), receiver.getDiscordChannelId());
            // 2. 构造鉴权头：Discord 使用 Bot 前缀
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bot " + receiver.getDiscordBotToken());
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<DiscordNotifyDTO> request = new HttpEntity<>(notifyBody, headers);
            // 3. POST 并校验响应中是否含消息 ID
            ResponseEntity<DiscordResponseDTO> entity = restTemplate.postForEntity(url, request, DiscordResponseDTO.class);
            if (entity.getStatusCode() != HttpStatus.OK || entity.getBody() == null) {
                throw new AlertNoticeException("Http StatusCode " + entity.getStatusCode());
            }
            if (entity.getBody().id == null) {
                log.warn("Send Discord Bot failed: {}, code: {}",
                        entity.getBody().message, entity.getBody().code);
                throw new AlertNoticeException(entity.getBody().message);
            }
            log.debug("Send Discord Bot success");
        } catch (AlertNoticeException e) {
            throw e;
        } catch (Exception e) {
            throw new AlertNoticeException("[Discord Bot Notify Error] " + e.getMessage());
        }
    }

    /**
     * 返回 Discord 渠道类型枚举。
     *
     * @return {@link AlertNoticeTypeEnum#DISCORD}
     */
    @Override
    public AlertNoticeTypeEnum type() {
        return AlertNoticeTypeEnum.DISCORD;
    }

    /**
     * Discord 消息请求体。
     */
    @Data
    @Builder
    public static class DiscordNotifyDTO {
        /**
         * embed 消息列表
         */
        private List<EmbedDTO> embeds;
    }

    /**
     * Discord embed 内容。
     */
    @Data
    @Builder
    public static class EmbedDTO {
        /**
         * embed 标题
         */
        private String title;

        /**
         * embed 描述（正文）
         */
        private String description;
    }

    /**
     * Discord 响应体。
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DiscordResponseDTO {
        /**
         * 消息 ID，非空表示发送成功
         */
        private String id;

        /**
         * 消息类型
         */
        private Integer type;

        /**
         * 消息内容
         */
        private String content;

        /**
         * 错误信息
         */
        private String message;

        /**
         * 错误码
         */
        private Integer code;
    }
}
