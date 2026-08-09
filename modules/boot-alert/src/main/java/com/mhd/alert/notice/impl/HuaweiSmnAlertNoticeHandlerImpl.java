package com.mhd.alert.notice.impl;

import com.mhd.alert.entity.AlertGroup;
import com.mhd.alert.entity.NoticeReceiver;
import com.mhd.alert.entity.NoticeTemplate;
import com.mhd.alert.notice.AbstractAlertNoticeHandlerImpl;
import com.mhd.alert.notice.AlertNoticeException;
import com.mhd.alert.notice.AlertNoticeTypeEnum;
import com.mhd.boot.common.utils.encrypt.CryptoUtils;
import com.mhd.boot.common.utils.json.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.SimpleTimeZone;

/**
 * 华为云 SMN 渠道通知处理器。
 *
 * <p>通过华为云 SMN（Simple Message Notification）服务的 REST API 发送告警消息。
 * 不依赖华为云 SDK，直接使用 {@code RestTemplate} 调用 publishMessage 接口，
 * 采用 SDK-HMAC-SHA256 签名算法鉴权（与 {@link CryptoUtils#hmacSha256Hex} 配合）。
 *
 * <p>参考
 * <a href="https://support.huaweicloud.com/api-smn/smn_api_64002.html">SMN publishMessage API</a>。
 *
 * @author zhao-hao-dong
 */
@Component
@Slf4j
public class HuaweiSmnAlertNoticeHandlerImpl extends AbstractAlertNoticeHandlerImpl {

    /**
     * 华为云 IAM 鉴权算法标识
     */
    private static final String ALGORITHM = "SDK-HMAC-SHA256";

    /**
     * 华为云服务主机后缀
     */
    private static final String SMN_HOST_SUFFIX = ".myhuaweicloud.com";

    /**
     * 发送华为云 SMN 通知。
     *
     * <p>执行流程：
     * <ol>
     *   <li>渲染模板为消息正文并组装 publishMessage 请求体；</li>
     *   <li>计算 SDK-HMAC-SHA256 签名并构造 Authorization 头；</li>
     *   <li>POST 到 SMN 服务并校验 HTTP 状态码。</li>
     * </ol>
     *
     * @param receiver       通知接收人，含 smnAk/smnSk/smnProjectId/smnRegion/smnTopicUrn
     * @param noticeTemplate 通知模板，渲染为消息正文
     * @param alert          告警组，作为模板渲染数据来源
     * @throws AlertNoticeException 签名失败或 HTTP 调用失败时抛出
     */
    @Override
    public void send(NoticeReceiver receiver, NoticeTemplate noticeTemplate, AlertGroup alert) throws AlertNoticeException {
        try {
            // 1. 渲染模板并组装请求体
            Map<String, String> body = new HashMap<>(4);
            body.put("subject", NOTIFY_TITLE);
            body.put("message", renderContent(noticeTemplate, alert));
            String bodyJson = JsonUtils.toJsonString(body);
            // 2. 构造请求 URL（按 region 与 topicUrn）
            String url = buildSmnUrl(receiver);
            // 3. 计算 SDK-HMAC-SHA256 签名
            String timestamp = getCanonicalTimestamp();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Sdk-Date", timestamp);
            String authorization = buildAuthorization(receiver, "POST", url, timestamp, bodyJson);
            headers.set("Authorization", authorization);
            // 4. POST 到 SMN 服务并校验状态码
            HttpEntity<String> entity = new HttpEntity<>(bodyJson, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            if (response.getStatusCode().value() >= HttpStatus.BAD_REQUEST.value()) {
                log.warn("Send Huawei SMN failed: status={}, body={}", response.getStatusCode(), response.getBody());
                throw new AlertNoticeException("Huawei SMN http status " + response.getStatusCode());
            }
            log.debug("Send Huawei SMN success, topicUrn: {}", receiver.getSmnTopicUrn());
        } catch (AlertNoticeException e) {
            throw e;
        } catch (Exception e) {
            throw new AlertNoticeException("[Huawei SMN Notify Error] " + e.getMessage());
        }
    }

    /**
     * 构建 SMN publishMessage 请求 URL。
     *
     * @param receiver 含 smnRegion 与 smnTopicUrn 的接收人
     * @return 完整的请求 URL
     */
    private String buildSmnUrl(NoticeReceiver receiver) {
        // https://smn.{region}.myhuaweicloud.com/v2/{projectId}/notifications/topics/{topicUrn}/publish
        return "https://smn." + receiver.getSmnRegion() + SMN_HOST_SUFFIX
                + "/v2/" + receiver.getSmnProjectId()
                + "/notifications/topics/" + receiver.getSmnTopicUrn() + "/publish";
    }

    /**
     * 生成华为云规范的 ISO 8601 时间戳（yyyyMMddTHHmmssZ）。
     *
     * @return 规范化时间戳字符串
     */
    private String getCanonicalTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        sdf.setTimeZone(new SimpleTimeZone(0, "GMT"));
        return sdf.format(new Date());
    }

    /**
     * 计算 SDK-HMAC-SHA256 签名并构造 Authorization 头。
     *
     * <p>执行流程：
     * <ol>
     *   <li>计算请求体 SHA256 摘要并 Base64 编码；</li>
     *   <li>构建规范化请求（方法 + URI + 头部 + 摘要）；</li>
     *   <li>构建签名串（算法 + 时间 + 哈希后的规范请求）；</li>
     *   <li>用 SK 对签名串做 HMAC-SHA256 得到签名；</li>
     *   <li>拼装 {@code SDK-HMAC-SHA256 Access=...,SignedHeaders=...,Signature=...}。</li>
     * </ol>
     *
     * @param receiver  含 smnAk（AccessKey）与 smnSk（SecretKey）的接收人
     * @param method    HTTP 方法
     * @param url       完整请求 URL
     * @param timestamp 规范化时间戳
     * @param body      请求体 JSON
     * @return Authorization 头值
     * @throws Exception 签名计算失败时抛出
     */
    private String buildAuthorization(NoticeReceiver receiver, String method, String url,
                                      String timestamp, String body) throws Exception {
        // 1. 计算请求体 SHA256 摘要并 Base64 编码
        String contentHash = CryptoUtils.sha256Hex(body);
        String contentSha256 = Base64.getEncoder().encodeToString(contentHash.getBytes(StandardCharsets.UTF_8));
        // 2. 解析 URL 获取主机与 URI 路径
        URI uri = URI.create(url);
        String host = uri.getHost();
        String path = uri.getPath();
        // 3. 构建规范化请求：方法 + 路径 + 空查询串 + 头部 + 摘要
        String canonicalHeaders = "content-type:application/json\nhost:" + host + "\nx-sdk-date:" + timestamp + "\n";
        String signedHeaders = "content-type;host;x-sdk-date";
        String canonicalRequest = method + "\n" + path + "\n\n"
                + canonicalHeaders + "\n" + signedHeaders + "\n" + contentSha256;
        // 4. 构建签名串：算法 + 时间 + 哈希后的规范请求
        String hashCanonicalRequest = CryptoUtils.sha256Hex(canonicalRequest);
        String stringToSign = ALGORITHM + "\n" + timestamp + "\n" + hashCanonicalRequest;
        // 5. 用 SK 对签名串做 HMAC-SHA256 得到十六进制签名
        String signature = CryptoUtils.hmacSha256Hex(receiver.getSmnSk(), stringToSign);
        return ALGORITHM + " Access=" + receiver.getSmnAk()
                + ", SignedHeaders=" + signedHeaders
                + ", Signature=" + signature;
    }

    /**
     * 返回华为云 SMN 渠道类型枚举。
     *
     * @return {@link AlertNoticeTypeEnum#HUAWEI_SMN}
     */
    @Override
    public AlertNoticeTypeEnum type() {
        return AlertNoticeTypeEnum.HUAWEI_SMN;
    }
}
