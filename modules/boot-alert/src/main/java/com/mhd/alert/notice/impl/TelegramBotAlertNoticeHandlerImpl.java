package com.mhd.alert.notice.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mhd.alert.entity.AlertGroup;
import com.mhd.alert.entity.NoticeReceiver;
import com.mhd.alert.entity.NoticeTemplate;
import com.mhd.alert.notice.AbstractAlertNoticeHandlerImpl;
import com.mhd.alert.notice.AlertNoticeException;
import com.mhd.alert.notice.AlertNoticeTypeEnum;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Telegram Bot 渠道通知处理器。
 *
 * <p>通过 Telegram Bot API 的 {@code sendMessage} 接口发送告警文本消息。
 * 请求 URL 由 {@link com.mhd.alert.config.AlertProperties#getTelegramWebhookUrl()}
 * 模板（含 {@code %s} 占位符）与 bot token 拼接而成。
 *
 * @author zhao-hao-dong
 */
@Component
@Slf4j
public class TelegramBotAlertNoticeHandlerImpl extends AbstractAlertNoticeHandlerImpl {

    /**
     * 发送 Telegram Bot 通知。
     *
     * <p>执行流程：
     * <ol>
     *   <li>校验 bot token 格式（{@code 数字:字母数字_-}）；</li>
     *   <li>渲染模板为消息正文，组装请求体（含 chatId、禁用预览等）；</li>
     *   <li>POST 到 Telegram API 并校验响应 {@code ok} 字段。</li>
     * </ol>
     *
     * @param receiver       通知接收人，{@code tgBotToken} 为 bot token，{@code tgUserId} 为 chatId
     * @param noticeTemplate 通知模板，渲染为消息正文
     * @param alert          告警组，作为模板渲染数据来源
     * @throws AlertNoticeException token 非法或 HTTP 调用失败时抛出
     */
    @Override
    public void send(NoticeReceiver receiver, NoticeTemplate noticeTemplate, AlertGroup alert) throws AlertNoticeException {
        try {
            // 1. 校验 bot token 格式
            String token = receiver.getTgBotToken();
            if (!isValidTelegramToken(token)) {
                throw new AlertNoticeException("Invalid Telegram Bot Token");
            }
            // 2. 渲染模板并组装请求体
            String url = String.format(alertProperties.getTelegramWebhookUrl(), token);
            TelegramBotNotifyDTO notifyBody = TelegramBotNotifyDTO.builder()
                    .chatId(receiver.getTgUserId())
                    .text(renderContent(noticeTemplate, alert))
                    .messageThreadId(receiver.getTgMessageThreadId())
                    .disableWebPagePreview(true)
                    .build();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<TelegramBotNotifyDTO> telegramEntity = new HttpEntity<>(notifyBody, headers);
            // 3. POST 到 Telegram API 并校验响应 ok 字段
            ResponseEntity<TelegramBotNotifyResponse> entity = restTemplate.postForEntity(
                    url, telegramEntity, TelegramBotNotifyResponse.class);
            if (entity.getStatusCode() != HttpStatus.OK || entity.getBody() == null) {
                throw new AlertNoticeException("Http StatusCode " + entity.getStatusCode());
            }
            if (!entity.getBody().ok) {
                log.warn("Send Telegram Bot failed: {}, error_code: {}",
                        entity.getBody().description, entity.getBody().errorCode);
                throw new AlertNoticeException(entity.getBody().description);
            }
            log.debug("Send Telegram Bot success");
        } catch (AlertNoticeException e) {
            throw e;
        } catch (Exception e) {
            throw new AlertNoticeException("[Telegram Bot Notify Error] " + e.getMessage());
        }
    }

    /**
     * 校验 Telegram Bot Token 格式。
     *
     * <p>真实 token 形如 {@code 110201543:AAHdqTcvCH1vGWJxfSeofSAs0K5PALDsaw}，
     * 即 {@code 数字:字母数字_短横线}。
     *
     * @param token 待校验的 token
     * @return true 表示格式合法
     */
    private boolean isValidTelegramToken(String token) {
        String tokenPattern = "^[0-9]+:[a-zA-Z0-9_-]+$";
        return token != null && token.matches(tokenPattern);
    }

    /**
     * 返回 Telegram 渠道类型枚举。
     *
     * @return {@link AlertNoticeTypeEnum#TELEGRAM}
     */
    @Override
    public AlertNoticeTypeEnum type() {
        return AlertNoticeTypeEnum.TELEGRAM;
    }

    /**
     * Telegram sendMessage 请求体。
     */
    @Data
    @Builder
    public static class TelegramBotNotifyDTO {
        /**
         * 目标 chat ID
         */
        @JsonProperty("chat_id")
        private String chatId;

        /**
         * 消息正文
         */
        private String text;

        /**
         * 是否禁用链接预览
         */
        @JsonProperty("disable_web_page_preview")
        private Boolean disableWebPagePreview;

        /**
         * 话题消息的 thread ID（论坛场景使用）
         */
        @JsonProperty("message_thread_id")
        private String messageThreadId;
    }

    /**
     * Telegram API 响应体。
     */
    @NoArgsConstructor
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TelegramBotNotifyResponse {
        /**
         * 是否成功
         */
        private boolean ok;

        /**
         * 错误码
         */
        @JsonProperty("error_code")
        private Integer errorCode;

        /**
         * 错误描述
         */
        private String description;
    }
}
