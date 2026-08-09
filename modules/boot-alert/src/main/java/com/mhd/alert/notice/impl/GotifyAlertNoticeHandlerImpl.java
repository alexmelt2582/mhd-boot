package com.mhd.alert.notice.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mhd.alert.entity.AlertGroup;
import com.mhd.alert.entity.NoticeReceiver;
import com.mhd.alert.entity.NoticeTemplate;
import com.mhd.alert.notice.AbstractAlertNoticeHandlerImpl;
import com.mhd.alert.notice.AlertNoticeException;
import com.mhd.alert.notice.AlertNoticeTypeEnum;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Gotify 渠道通知处理器。
 *
 * <p>通过自建 Gotify 服务器的推送接口发送告警消息。请求 URL 由
 * {@link com.mhd.alert.config.AlertProperties#getGotifyWebhookUrl()} 前缀
 * + {@code NoticeReceiver.gotifyToken} 拼接而成。支持 Markdown 内容类型。
 *
 * @author zhao-hao-dong
 */
@Component
@Slf4j
public class GotifyAlertNoticeHandlerImpl extends AbstractAlertNoticeHandlerImpl {

    /**
     * 发送 Gotify 通知。
     *
     * <p>执行流程：
     * <ol>
     *   <li>渲染模板为消息正文并组装请求体（含 title/message/extras）；</li>
     *   <li>POST 到 Gotify 推送接口（前缀 + token）；</li>
     *   <li>校验 HTTP 状态码。</li>
     * </ol>
     *
     * @param receiver       通知接收人，{@code gotifyToken} 为 Gotify 应用 token
     * @param noticeTemplate 通知模板，渲染为消息正文
     * @param alert          告警组，作为模板渲染数据来源
     * @throws AlertNoticeException HTTP 调用失败时抛出
     */
    @Override
    public void send(NoticeReceiver receiver, NoticeTemplate noticeTemplate, AlertGroup alert) throws AlertNoticeException {
        try {
            // 1. 渲染模板并组装请求体
            GotifyWebHookDto dto = new GotifyWebHookDto();
            dto.setTitle(NOTIFY_TITLE);
            dto.setMessage(renderContent(noticeTemplate, alert));
            // 设置内容类型为 markdown，使 Gotify 客户端按 markdown 渲染
            GotifyWebHookDto.ClientDisplay clientDisplay = new GotifyWebHookDto.ClientDisplay();
            clientDisplay.setContentType("text/markdown");
            GotifyWebHookDto.Extras extras = new GotifyWebHookDto.Extras();
            extras.setClientDisplay(clientDisplay);
            dto.setExtras(extras);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<GotifyWebHookDto> httpEntity = new HttpEntity<>(dto, headers);
            // 2. POST 到 Gotify 推送接口
            String webHookUrl = String.format(alertProperties.getGotifyWebhookUrl(), receiver.getGotifyToken());
            ResponseEntity<CommonRobotNotifyResp> responseEntity = restTemplate.postForEntity(
                    webHookUrl, httpEntity, CommonRobotNotifyResp.class);
            // 3. 校验 HTTP 状态码
            if (responseEntity.getStatusCode() != HttpStatus.OK) {
                log.warn("Send Gotify webHook: {} failed: {}", webHookUrl, responseEntity.getBody());
                throw new AlertNoticeException("Http StatusCode " + responseEntity.getStatusCode());
            }
            log.debug("Send Gotify webHook: {} success", webHookUrl);
        } catch (AlertNoticeException e) {
            throw e;
        } catch (Exception e) {
            throw new AlertNoticeException("[Gotify Notify Error] " + e.getMessage());
        }
    }

    /**
     * 返回 Gotify 渠道类型枚举。
     *
     * @return {@link AlertNoticeTypeEnum#GOTIFY}
     */
    @Override
    public AlertNoticeTypeEnum type() {
        return AlertNoticeTypeEnum.GOTIFY;
    }

    /**
     * Gotify 推送请求体。
     */
    @Data
    public static class GotifyWebHookDto {
        /**
         * 消息标题
         */
        private String title;

        /**
         * 消息正文
         */
        private String message;

        /**
         * 扩展信息（含内容类型等客户端显示配置）
         */
        private Extras extras;

        /**
         * Gotify 客户端显示扩展。
         */
        @Data
        public static class Extras {
            /**
             * 客户端显示配置
             */
            @JsonProperty("client::display")
            private ClientDisplay clientDisplay;
        }

        /**
         * Gotify 客户端显示配置。
         */
        @Data
        public static class ClientDisplay {
            /**
             * 内容类型，如 text/markdown
             */
            private String contentType;
        }
    }
}
