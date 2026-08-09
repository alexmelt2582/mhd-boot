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

import java.util.Map;
import java.util.StringJoiner;

/**
 * Ntfy 渠道通知处理器。
 *
 * <p>通过 ntfy 推送服务的 publish API 发送告警消息。支持优先级映射、emoji 标签、
 * 点击跳转 URL，以及自建服务器的 Bearer token 鉴权。
 *
 * <p>参考 <a href="https://docs.ntfy.sh/publish/">ntfy publish API 文档</a>。
 *
 * @author zhao-hao-dong
 */
@Component
@Slf4j
public class NtfyAlertNoticeHandlerImpl extends AbstractAlertNoticeHandlerImpl {

    /**
     * 告警触发状态标签
     */
    private static final String STATUS_FIRING = "firing";

    /**
     * 严重级别：critical
     */
    private static final String SEVERITY_CRITICAL = "critical";

    /**
     * 严重级别：warning
     */
    private static final String SEVERITY_WARNING = "warning";

    /**
     * 发送 Ntfy 通知。
     *
     * <p>执行流程：
     * <ol>
     *   <li>渲染模板为消息正文；</li>
     *   <li>构建 ntfy publish URL：服务器地址 + 主题；</li>
     *   <li>设置请求头：标题、Markdown、优先级、标签、点击跳转、鉴权；</li>
     *   <li>POST 并校验 HTTP 状态码。</li>
     * </ol>
     *
     * @param receiver       通知接收人，含 ntfyServerUrl/ntfyTopic/ntfyToken
     * @param noticeTemplate 通知模板，渲染为消息正文
     * @param alert          告警组，用于推导优先级与标签
     * @throws AlertNoticeException HTTP 调用失败时抛出
     */
    @Override
    public void send(NoticeReceiver receiver, NoticeTemplate noticeTemplate, AlertGroup alert) throws AlertNoticeException {
        try {
            // 1. 渲染模板为消息正文
            String content = renderContent(noticeTemplate, alert);
            String url = buildNtfyUrl(receiver);
            // 2. 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.set("Title", NOTIFY_TITLE);
            headers.set("Markdown", "yes");
            headers.set("Priority", String.valueOf(mapPriority(alert)));
            headers.set("Tags", buildTags(alert));
            // 设置点击跳转 URL 到控制台地址（如配置）
            if (alertProperties != null && StringUtils.isNotBlank(alertProperties.getConsoleUrl())) {
                headers.set("Click", alertProperties.getConsoleUrl());
            }
            // 自建 ntfy 服务器的 Bearer token 鉴权
            String token = receiver.getNtfyToken();
            if (StringUtils.isNotBlank(token)) {
                headers.set("Authorization", "Bearer " + token);
            }
            // 3. POST 并校验状态码
            HttpEntity<String> httpEntity = new HttpEntity<>(content, headers);
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, httpEntity, String.class);
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                log.debug("Send ntfy notification to {} success", url);
            } else {
                log.warn("Send ntfy notification to {} failed: {}", url, responseEntity.getBody());
                throw new AlertNoticeException("Http StatusCode " + responseEntity.getStatusCode());
            }
        } catch (AlertNoticeException e) {
            throw e;
        } catch (Exception e) {
            throw new AlertNoticeException("[Ntfy Notify Error] " + e.getMessage());
        }
    }

    /**
     * 构建 ntfy publish URL：服务器地址（去除尾部斜杠）+ 主题。
     *
     * @param receiver 含 ntfyServerUrl 与 ntfyTopic 的接收人
     * @return 完整的 publish URL
     */
    private String buildNtfyUrl(NoticeReceiver receiver) {
        // 服务器地址为空时回退到默认地址
        String serverUrl = receiver.getNtfyServerUrl();
        if (StringUtils.isBlank(serverUrl)) {
            serverUrl = alertProperties.getNtfyDefaultServerUrl();
        }
        // 去除尾部斜杠避免拼接出双斜杠
        if (serverUrl.endsWith("/")) {
            serverUrl = serverUrl.substring(0, serverUrl.length() - 1);
        }
        return serverUrl + "/" + receiver.getNtfyTopic();
    }

    /**
     * 将告警严重级别映射为 ntfy 优先级（1-5）。
     *
     * <p>映射规则：
     * <ul>
     *   <li>5（max）：critical 级别且 firing 状态</li>
     *   <li>4（high）：warning 级别且 firing 状态</li>
     *   <li>3（default）：info 或未知级别且 firing 状态</li>
     *   <li>2（low）：已恢复（resolved）告警</li>
     * </ul>
     *
     * @param alert 告警组
     * @return ntfy 优先级数值
     */
    protected int mapPriority(AlertGroup alert) {
        // 已恢复告警统一为低优先级
        if (!STATUS_FIRING.equalsIgnoreCase(alert.getStatus())) {
            return 2;
        }
        String severity = extractSeverity(alert);
        if (SEVERITY_CRITICAL.equalsIgnoreCase(severity)) {
            return 5;
        } else if (SEVERITY_WARNING.equalsIgnoreCase(severity)) {
            return 4;
        }
        return 3;
    }

    /**
     * 构建基于告警状态与严重级别的 emoji 标签串。
     *
     * <p>firing+critical → 🚨💀；firing+warning → ⚠️；firing+其他 → ℹ️；resolved → ✅。
     * 同时追加告警名作为纯文本标签。
     *
     * @param alert 告警组
     * @return 逗号分隔的标签串
     */
    protected String buildTags(AlertGroup alert) {
        StringJoiner joiner = new StringJoiner(",");
        String status = alert.getStatus();
        if (STATUS_FIRING.equalsIgnoreCase(status)) {
            String severity = extractSeverity(alert);
            if (SEVERITY_CRITICAL.equalsIgnoreCase(severity)) {
                joiner.add("rotating_light");
                joiner.add("skull");
            } else if (SEVERITY_WARNING.equalsIgnoreCase(severity)) {
                joiner.add("warning");
            } else {
                joiner.add("information_source");
            }
        } else {
            // 已恢复告警显示绿色对勾
            joiner.add("white_check_mark");
        }
        // 追加告警名作为纯文本标签
        Map<String, String> commonLabels = alert.getCommonLabels();
        if (commonLabels != null && commonLabels.containsKey("alertname")) {
            joiner.add(commonLabels.get("alertname"));
        }
        return joiner.toString();
    }

    /**
     * 从告警公共标签提取严重级别：优先 severity 键，回退 priority 键。
     *
     * @param alert 告警组
     * @return 严重级别字符串，可能为 null
     */
    private String extractSeverity(AlertGroup alert) {
        Map<String, String> commonLabels = alert.getCommonLabels();
        if (commonLabels == null) {
            return null;
        }
        String severity = commonLabels.get("severity");
        if (severity == null) {
            severity = commonLabels.get("priority");
        }
        return severity;
    }

    /**
     * 返回 Ntfy 渠道类型枚举。
     *
     * @return {@link AlertNoticeTypeEnum#NTFY}
     */
    @Override
    public AlertNoticeTypeEnum type() {
        return AlertNoticeTypeEnum.NTFY;
    }
}
