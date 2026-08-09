package com.mhd.alert.notice.impl;

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

import java.util.List;

/**
 * Server 酱渠道通知处理器。
 *
 * <p>通过 Server 酱的推送接口发送告警消息。请求 URL 由
 * {@link com.mhd.alert.config.AlertProperties#getServerChanWebhookUrl()} 模板（含 {@code %s}）
 * 与 token 拼接而成。token 仅允许字母、数字、下划线、短横线，防止 URL 注入。
 *
 * @author zhao-hao-dong
 */
@Component
@Slf4j
public class ServerChanAlertNoticeHandlerImpl extends AbstractAlertNoticeHandlerImpl {

    /**
     * 允许的 Server 酱 API 域名白名单，防止 URL 注入
     */
    private static final List<String> ALLOWED_BASE_URLS = List.of(
            "https://sctapi.ftqq.com",
            "https://api.serverchan.com"
    );

    /**
     * 发送 Server 酱通知。
     *
     * <p>执行流程：
     * <ol>
     *   <li>对 token 做白名单字符过滤（仅保留字母数字下划线短横线）；</li>
     *   <li>拼接 URL 并校验是否属于允许的域名白名单；</li>
     *   <li>渲染模板为正文并组装请求体；</li>
     *   <li>POST 到 Server 酱接口并校验 HTTP 状态码。</li>
     * </ol>
     *
     * @param receiver       通知接收人，{@code serverChanToken} 为 Server 酱 token
     * @param noticeTemplate 通知模板，渲染为消息正文
     * @param alert          告警组，作为模板渲染数据来源
     * @throws AlertNoticeException URL 校验失败或 HTTP 调用失败时抛出
     */
    @Override
    public void send(NoticeReceiver receiver, NoticeTemplate noticeTemplate, AlertGroup alert) throws AlertNoticeException {
        try {
            // 1. 对 token 做白名单字符过滤，防止 URL 注入
            String sanitizedToken = receiver.getServerChanToken().replaceAll("[^a-zA-Z0-9_-]", "");
            String webHookUrl = String.format(alertProperties.getServerChanWebhookUrl(), sanitizedToken);
            // 2. 校验 URL 是否属于允许的域名白名单
            boolean isValidUrl = ALLOWED_BASE_URLS.stream().anyMatch(webHookUrl::startsWith);
            if (!isValidUrl) {
                throw new AlertNoticeException("Invalid ServerChan webhook URL: " + webHookUrl);
            }
            // 3. 渲染模板并组装请求体
            ServerChanWebHookDto dto = new ServerChanWebHookDto();
            dto.setTitle(NOTIFY_TITLE);
            dto.setDesp(renderContent(noticeTemplate, alert));
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<ServerChanWebHookDto> httpEntity = new HttpEntity<>(dto, headers);
            // 4. POST 并校验 HTTP 状态码
            ResponseEntity<CommonRobotNotifyResp> responseEntity = restTemplate.postForEntity(
                    webHookUrl, httpEntity, CommonRobotNotifyResp.class);
            if (responseEntity.getStatusCode() != HttpStatus.OK) {
                log.warn("Send ServerChan webHook: {} failed: {}", webHookUrl, responseEntity.getBody());
                throw new AlertNoticeException("Http StatusCode " + responseEntity.getStatusCode());
            }
            log.debug("Send ServerChan webHook: {} success", webHookUrl);
        } catch (AlertNoticeException e) {
            throw e;
        } catch (Exception e) {
            throw new AlertNoticeException("[ServerChan Notify Error] " + e.getMessage());
        }
    }

    /**
     * 返回 Server 酱渠道类型枚举。
     *
     * @return {@link AlertNoticeTypeEnum#SERVER_CHAN}
     */
    @Override
    public AlertNoticeTypeEnum type() {
        return AlertNoticeTypeEnum.SERVER_CHAN;
    }

    /**
     * Server 酱请求体。
     */
    @Data
    public static class ServerChanWebHookDto {
        /**
         * 消息标题
         */
        private String title;

        /**
         * 消息正文（支持 Markdown）
         */
        private String desp;
    }
}
