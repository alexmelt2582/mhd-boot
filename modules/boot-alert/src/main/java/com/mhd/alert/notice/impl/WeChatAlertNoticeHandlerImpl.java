package com.mhd.alert.notice.impl;

import com.mhd.alert.entity.AlertGroup;
import com.mhd.alert.entity.NoticeReceiver;
import com.mhd.alert.entity.NoticeTemplate;
import com.mhd.alert.notice.AbstractAlertNoticeHandlerImpl;
import com.mhd.alert.notice.AlertNoticeException;
import com.mhd.alert.notice.AlertNoticeTypeEnum;
import com.mhd.boot.common.utils.json.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 微信公众号渠道通知处理器。
 *
 * <p>通过企业微信「应用消息」接口发送告警文本消息。流程：先用 corpId + corpSecret
 * 换取 access_token，再将渲染后的告警内容 POST 到消息发送接口。
 *
 * <p>注意：本处理器对应 {@code NoticeReceiver.type=3}（微信公众号），
 * 与 {@code WeComAppAlertNoticeHandlerImpl}（type=10，企微应用消息）的实现存在重叠，
 * 实际项目中可按需合并。此处保留独立实现以兼容历史配置。
 *
 * @author zhao-hao-dong
 */
@Component
@Slf4j
public class WeChatAlertNoticeHandlerImpl extends AbstractAlertNoticeHandlerImpl {

    /**
     * 获取 access_token 的接口 URL 模板
     */
    private static final String GET_TOKEN_URL = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=%s&corpsecret=%s";

    /**
     * 发送应用消息的接口 URL
     */
    private static final String SEND_MESSAGE_URL = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=";

    /**
     * 发送微信公众号通知。
     *
     * <p>执行流程：
     * <ol>
     *   <li>用 corpId + appSecret 换取 access_token；</li>
     *   <li>渲染通知模板并组装 {@code {msgtype:text, text:{content}}} 请求体；</li>
     *   <li>POST 到消息发送接口并校验 errcode。</li>
     * </ol>
     *
     * @param receiver       通知接收人，{@code corpId} 与 {@code appSecret} 用于换 token
     * @param noticeTemplate 通知模板，渲染为消息正文
     * @param alert          告警组，作为模板渲染数据来源
     * @throws AlertNoticeException 换 token 或发送失败时抛出
     */
    @Override
    public void send(NoticeReceiver receiver, NoticeTemplate noticeTemplate, AlertGroup alert) throws AlertNoticeException {
        try {
            // 1. 用 corpId + appSecret 换取 access_token
            String accessToken = getAccessToken(receiver.getCorpId(), receiver.getAppSecret());
            // 2. 渲染通知模板并组装请求体
            String content = renderContent(noticeTemplate, alert);
            Map<String, Object> textContent = new HashMap<>(2);
            textContent.put("content", content);
            Map<String, Object> messageBody = new HashMap<>(4);
            messageBody.put("msgtype", "text");
            messageBody.put("text", textContent);
            // 3. POST 到消息发送接口并校验 errcode
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(JsonUtils.toJsonString(messageBody), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    SEND_MESSAGE_URL + accessToken, entity, String.class);
            if (response.getStatusCode() == HttpStatus.OK
                    && response.getBody() != null
                    && response.getBody().contains("\"errcode\":0")) {
                log.debug("Send WeChat message success");
            } else {
                log.warn("Send WeChat message failed: {}", response.getBody());
                throw new AlertNoticeException("WeChat send failed: " + response.getBody());
            }
        } catch (AlertNoticeException e) {
            throw e;
        } catch (Exception e) {
            throw new AlertNoticeException("[WeChat Notify Error] " + e.getMessage());
        }
    }

    /**
     * 用 corpId + corpSecret 换取企业微信 access_token。
     *
     * @param corpId     企业 ID
     * @param corpSecret 应用 Secret
     * @return access_token 字符串
     * @throws AlertNoticeException HTTP 调用失败或响应不含 access_token 时抛出
     */
    private String getAccessToken(String corpId, String corpSecret) throws Exception {
        // 使用 JDK HttpClient 拉取 token，避免对响应体做复杂反序列化
        HttpClient client = HttpClient.newHttpClient();
        String url = String.format(GET_TOKEN_URL, corpId, corpSecret);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // 简单解析：从响应 JSON 中提取 access_token 字段值
        String body = response.body();
        if (body == null || !body.contains("access_token")) {
            throw new AlertNoticeException("Failed to obtain access_token: " + body);
        }
        // 通过 JsonUtils 解析获取 access_token
        var tokenNode = JsonUtils.parseTree(body);
        String token = Objects.requireNonNull(tokenNode).get("access_token").asText();
        log.debug("Obtained WeChat access_token successfully");
        return token;
    }

    /**
     * 返回微信公众号渠道类型枚举。
     *
     * @return {@link AlertNoticeTypeEnum#WE_CHAT}
     */
    @Override
    public AlertNoticeTypeEnum type() {
        return AlertNoticeTypeEnum.WE_CHAT;
    }
}
