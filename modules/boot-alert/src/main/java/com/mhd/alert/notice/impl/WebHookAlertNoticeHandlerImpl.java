package com.mhd.alert.notice.impl;

import com.mhd.alert.entity.AlertGroup;
import com.mhd.alert.entity.NoticeReceiver;
import com.mhd.alert.entity.NoticeTemplate;
import com.mhd.alert.notice.AbstractAlertNoticeHandlerImpl;
import com.mhd.alert.notice.AlertNoticeException;
import com.mhd.alert.notice.AlertNoticeTypeEnum;
import com.mhd.boot.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.net.URI;

/**
 * WebHook 渠道通知处理器。
 *
 * <p>将告警内容渲染为 JSON 后 POST 到用户配置的 webhook URL，支持 Basic / Bearer
 * 两种鉴权方式。适用于自定义告警接收端（如自建回调服务、第三方接入等）。
 *
 * <p>注意：使用 {@link URI} 重载的 {@code postForEntity} 而非 String 重载，
 * 避免 String 重载将 URL 视为 URI 模板并对预编码的查询参数二次编码导致 URL 损坏。
 *
 * @author zhao-hao-dong
 */
@Component
@Slf4j
public class WebHookAlertNoticeHandlerImpl extends AbstractAlertNoticeHandlerImpl {

    /**
     * 发送 WebHook 通知。
     *
     * <p>执行流程：
     * <ol>
     *   <li>校验 webhook URL 非空，空则抛异常；</li>
     *   <li>将 URL 转为 {@link URI}，避免 String 重载的二次编码问题；</li>
     *   <li>按 {@code hookAuthType} 设置 Basic / Bearer 鉴权头；</li>
     *   <li>渲染模板为 JSON 请求体并清理尾随逗号；</li>
     *   <li>POST 请求并校验响应状态码。</li>
     * </ol>
     *
     * @param receiver       通知接收人，包含 webhook URL 与鉴权信息
     * @param noticeTemplate 通知模板，{@code content} 为 JSON 模板字符串
     * @param alert          告警组，作为模板渲染数据来源
     * @throws AlertNoticeException URL 非法或 HTTP 调用失败时抛出
     */
    @Override
    public void send(NoticeReceiver receiver, NoticeTemplate noticeTemplate, AlertGroup alert) throws AlertNoticeException {
        try {
            // 1. 校验 webhook URL 非空
            String hookUrl = receiver.getHookUrl();
            if (StringUtils.isBlank(hookUrl)) {
                throw new AlertNoticeException("Webhook URL is null or empty");
            }
            // 2. 转为 URI 避免 String 重载的 URI 模板二次编码问题
            URI hookUri;
            try {
                hookUri = URI.create(hookUrl);
            } catch (IllegalArgumentException e) {
                throw new AlertNoticeException("Invalid webhook URL: " + e.getMessage());
            }
            // 3. 按鉴权类型设置 Authorization 头
            HttpHeaders headers = new HttpHeaders();
            if ("Basic".equalsIgnoreCase(receiver.getHookAuthType())) {
                headers.setBasicAuth(receiver.getHookAuthToken());
            } else if ("Bearer".equalsIgnoreCase(receiver.getHookAuthType())) {
                headers.setBearerAuth(receiver.getHookAuthToken());
            }
            headers.setContentType(MediaType.APPLICATION_JSON);
            // 4. 渲染模板为 JSON 并清理尾随逗号（FreeMarker 模板易产生 ",\n  }" 这种尾随逗号）
            String webhookJson = renderContent(noticeTemplate, alert);
            webhookJson = webhookJson.replace(",\n  }", "\n }");
            // 5. POST 请求并校验响应
            HttpEntity<String> alertHttpEntity = new HttpEntity<>(webhookJson, headers);
            ResponseEntity<String> entity = restTemplate.postForEntity(hookUri, alertHttpEntity, String.class);
            if (entity.getStatusCode().value() < HttpStatus.BAD_REQUEST.value()) {
                log.debug("Send WebHook: {} success", hookUrl);
            } else {
                log.warn("Send WebHook: {} failed: {}", hookUrl, entity.getBody());
                throw new AlertNoticeException("Http StatusCode " + entity.getStatusCode());
            }
        } catch (AlertNoticeException e) {
            throw e;
        } catch (Exception e) {
            throw new AlertNoticeException("[WebHook Notify Error] " + e.getMessage());
        }
    }

    /**
     * 返回 WebHook 渠道类型枚举。
     *
     * @return {@link AlertNoticeTypeEnum#WEB_HOOK}
     */
    @Override
    public AlertNoticeTypeEnum type() {
        return AlertNoticeTypeEnum.WEB_HOOK;
    }
}
