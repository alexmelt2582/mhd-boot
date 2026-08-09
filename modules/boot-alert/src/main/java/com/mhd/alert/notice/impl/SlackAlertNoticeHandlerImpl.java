package com.mhd.alert.notice.impl;

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

import java.net.URI;
import java.util.Objects;

/**
 * Slack 渠道通知处理器。
 *
 * <p>通过 Slack Incoming Webhook 发送告警文本消息。webhook URL 由接收人配置
 * （{@code NoticeReceiver.slackWebHookUrl}），且必须属于 {@code hooks.slack.com} 域名
 * 以防 SSRF 风险。
 *
 * @author zhao-hao-dong
 */
@Component
@Slf4j
public class SlackAlertNoticeHandlerImpl extends AbstractAlertNoticeHandlerImpl {

    /**
     * Slack webhook 成功响应体固定为字符串 "ok"
     */
    private static final String SUCCESS = "ok";

    /**
     * 发送 Slack 通知。
     *
     * <p>执行流程：
     * <ol>
     *   <li>校验 webhook URL 属于 {@code hooks.slack.com} 域名；</li>
     *   <li>渲染模板为消息正文并组装请求体；</li>
     *   <li>POST 到 webhook URL 并校验响应体是否为 "ok"。</li>
     * </ol>
     *
     * @param receiver       通知接收人，{@code slackWebHookUrl} 为 Slack webhook 地址
     * @param noticeTemplate 通知模板，渲染为消息正文
     * @param alert          告警组，作为模板渲染数据来源
     * @throws AlertNoticeException URL 非法或发送失败时抛出
     */
    @Override
    public void send(NoticeReceiver receiver, NoticeTemplate noticeTemplate, AlertGroup alert) throws AlertNoticeException {
        try {
            // 1. 校验 webhook URL 域名，防止 SSRF
            String slackWebHookUrl = receiver.getSlackWebHookUrl();
            if (!isValidSlackWebHookUrl(slackWebHookUrl)) {
                log.warn("Invalid Slack Webhook URL: {}", slackWebHookUrl);
                throw new AlertNoticeException("Invalid Slack Webhook URL");
            }
            // 2. 渲染模板并组装请求体
            SlackNotifyDTO slackNotify = SlackNotifyDTO.builder()
                    .text(renderContent(noticeTemplate, alert))
                    .build();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<SlackNotifyDTO> slackNotifyEntity = new HttpEntity<>(slackNotify, headers);
            // 3. POST 到 webhook URL 并校验响应体
            ResponseEntity<String> entity = restTemplate.postForEntity(slackWebHookUrl, slackNotifyEntity, String.class);
            if (entity.getStatusCode() != HttpStatus.OK || entity.getBody() == null) {
                throw new AlertNoticeException("Http StatusCode " + entity.getStatusCode());
            }
            if (!Objects.equals(SUCCESS, entity.getBody())) {
                log.warn("Send Slack failed: {}", entity.getBody());
                throw new AlertNoticeException(entity.getBody());
            }
            log.debug("Send Slack success");
        } catch (AlertNoticeException e) {
            throw e;
        } catch (Exception e) {
            throw new AlertNoticeException("[Slack Notify Error] " + e.getMessage());
        }
    }

    /**
     * 校验 Slack Webhook URL 是否属于允许的域名。
     *
     * @param url 待校验的 URL
     * @return true 表示 host 为 {@code hooks.slack.com}
     */
    private boolean isValidSlackWebHookUrl(String url) {
        try {
            URI uri = new URI(url);
            return "hooks.slack.com".equals(uri.getHost());
        } catch (Exception e) {
            log.warn("Error validating Slack Webhook URL: {}", url, e);
            return false;
        }
    }

    /**
     * 返回 Slack 渠道类型枚举。
     *
     * @return {@link AlertNoticeTypeEnum#SLACK}
     */
    @Override
    public AlertNoticeTypeEnum type() {
        return AlertNoticeTypeEnum.SLACK;
    }

    /**
     * Slack webhook 请求体。
     */
    @Data
    @Builder
    public static class SlackNotifyDTO {
        /**
         * 消息正文
         */
        private String text;
    }
}
