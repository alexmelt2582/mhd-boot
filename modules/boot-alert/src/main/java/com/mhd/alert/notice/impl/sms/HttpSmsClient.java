package com.mhd.alert.notice.impl.sms;

import com.mhd.alert.entity.AlertGroup;
import com.mhd.alert.entity.NoticeReceiver;
import com.mhd.alert.entity.NoticeTemplate;
import com.mhd.alert.notice.AlertNoticeException;
import com.mhd.boot.common.utils.json.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 通用 HTTP 短信网关客户端。
 *
 * <p>适用于自建或第三方短信网关，只要支持 HTTP POST + JSON 请求体即可对接。
 * 请求体格式：{@code {"phone": "...", "content": "..."}}，鉴权通过
 * {@code Authorization: Bearer <token>} 头携带。
 *
 * <p>使用示例（{@code application.yml}）：
 * <pre>{@code
 * com:
 *   mhd:
 *     alert:
 *       sms:
 *         enable: true
 *         type: http
 *         http:
 *           url: https://sms.example.com/send
 *           token: ****
 * }</pre>
 *
 * <p>注意：本实现使用 {@link RestTemplate}（由 Spring 注入），与抽象基类
 * {@code AbstractAlertNoticeHandlerImpl} 共享同一个实例。
 *
 * @author zhao-hao-dong
 */
@Slf4j
public class HttpSmsClient implements SmsClient {

    private final String url;
    private final String token;
    private final RestTemplate restTemplate;

    /**
     * 用 HTTP 网关配置构造客户端。
     *
     * @param config       HTTP 短信网关配置
     * @param restTemplate Spring 注入的 HTTP 客户端
     */
    public HttpSmsClient(SmsProperties.HttpProperties config, RestTemplate restTemplate) {
        this.url = config == null ? null : config.getUrl();
        this.token = config == null ? null : config.getToken();
        this.restTemplate = restTemplate;
    }

    /**
     * 发送短信通知。
     *
     * <p>执行流程：
     * <ol>
     *   <li>从告警公共注解提取短信正文（summary → description → 首个注解值）；</li>
     *   <li>组装 {@code {phone, content}} JSON 请求体；</li>
     *   <li>POST 到网关 URL，校验 HTTP 状态码。</li>
     * </ol>
     *
     * @param receiver       通知接收人，{@code phone} 字段为手机号
     * @param noticeTemplate 通知模板（HTTP 网关模式下未直接使用，正文来自告警注解）
     * @param alert          告警组，作为短信内容数据来源
     * @throws AlertNoticeException 发送失败时抛出
     */
    @Override
    public void sendMessage(NoticeReceiver receiver, NoticeTemplate noticeTemplate, AlertGroup alert) {
        try {
            // 1. 从公共注解提取短信正文：优先 summary，其次 description，最后取首个注解值
            String content = null;
            if (alert.getCommonAnnotations() != null) {
                content = alert.getCommonAnnotations().get("summary");
                if (content == null) {
                    content = alert.getCommonAnnotations().get("description");
                }
                if (content == null) {
                    content = alert.getCommonAnnotations().values().stream().findFirst().orElse(null);
                }
            }
            if (content == null) {
                content = alert.getGroupKey();
            }
            // 2. 组装请求体
            Map<String, String> body = new HashMap<>(4);
            body.put("phone", receiver.getPhone());
            body.put("content", content);
            // 3. 构造请求头：JSON + Bearer 鉴权
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (token != null && !token.isBlank()) {
                headers.setBearerAuth(token);
            }
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            // 4. 校验响应状态码
            if (response.getStatusCode().value() >= HttpStatus.BAD_REQUEST.value()) {
                throw new AlertNoticeException("HTTP SMS gateway failed, status: " + response.getStatusCode()
                        + ", body: " + response.getBody());
            }
            log.info("Send SMS via HTTP gateway success, phone: {}", receiver.getPhone());
        } catch (AlertNoticeException e) {
            throw e;
        } catch (Exception e) {
            throw new AlertNoticeException("[HTTP SMS Error] " + e.getMessage());
        }
    }

    @Override
    public String getType() {
        return SmsClientFactory.TYPE_HTTP;
    }

    @Override
    public boolean checkConfig() {
        // URL 必填，token 可选（部分网关无需鉴权）
        return url != null && !url.isBlank();
    }
}
