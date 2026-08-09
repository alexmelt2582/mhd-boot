package com.mhd.alert.notice.impl.sms;

import com.mhd.alert.entity.AlertGroup;
import com.mhd.alert.entity.NoticeReceiver;
import com.mhd.alert.entity.NoticeTemplate;
import com.mhd.alert.notice.AlertNoticeException;
import com.mhd.boot.common.utils.encrypt.CryptoUtils;
import com.mhd.boot.common.utils.json.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import tools.jackson.databind.JsonNode;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.TreeMap;
import java.util.UUID;

/**
 * 阿里云短信客户端实现。
 *
 * <p>基于阿里云 dysmsapi 服务的 SendSms 接口，采用 ACS3-HMAC-SHA256 签名算法。
 * 参考 <a href="https://next.api.aliyun.com/document/Dysmsapi/2017-05-25/SendSms">SendSms API 文档</a>。
 *
 * <p>短信内容通过模板变量 {@code instance}/{@code priority}/{@code content} 渲染，
 * 模板需在阿里云控制台提前创建并通过审核。
 *
 * <p>使用示例（{@code application.yml}）：
 * <pre>{@code
 * com:
 *   mhd:
 *     alert:
 *       sms:
 *         enable: true
 *         type: alibaba
 *         alibaba:
 *           access-key-id: LTAI****
 *           access-key-secret: ****
 *           sign-name: 监控告警
 *           template-code: SMS_****
 * }</pre>
 *
 * @author zhao-hao-dong
 */
@Slf4j
public class AliyunSmsClient implements SmsClient {

    /**
     * 阿里云 dysmsapi API 版本
     */
    private static final String API_VERSION = "2017-05-25";

    /**
     * SendSms 动作名
     */
    private static final String ACTION = "SendSms";

    /**
     * 阿里云 dysmsapi 服务主机
     */
    private static final String HOST = "dysmsapi.aliyuncs.com";

    /**
     * ACS3 签名算法标识
     */
    private static final String ALGORITHM = "ACS3-HMAC-SHA256";

    private final String accessKeyId;
    private final String accessKeySecret;
    private final String signName;
    private final String templateCode;

    /**
     * 用阿里云 SMS 配置构造客户端。
     *
     * @param config 阿里云短信配置；为 null 时各字段初始化为空字符串，{@link #checkConfig()} 返回 false
     */
    public AliyunSmsClient(SmsProperties.AlibabaProperties config) {
        if (config != null) {
            this.accessKeyId = config.getAccessKeyId();
            this.accessKeySecret = config.getAccessKeySecret();
            this.signName = config.getSignName();
            this.templateCode = config.getTemplateCode();
        } else {
            this.accessKeyId = "";
            this.accessKeySecret = "";
            this.signName = "";
            this.templateCode = "";
        }
    }

    /**
     * 发送短信通知。
     *
     * <p>执行流程：
     * <ol>
     *   <li>从告警公共标签与注解中提取 instance、priority、content 三个模板变量；</li>
     *   <li>组装模板变量 JSON 并委托 {@link #sendSms(String, String)} 发送；</li>
     *   <li>发送过程中的异常统一抛 {@link AlertNoticeException}。</li>
     * </ol>
     *
     * @param receiver       通知接收人，{@code phone} 字段为手机号
     * @param noticeTemplate 通知模板（阿里云短信使用模板 Code，此处未直接使用）
     * @param alert          告警组，作为短信内容数据来源
     */
    @Override
    public void sendMessage(NoticeReceiver receiver, NoticeTemplate noticeTemplate, AlertGroup alert) {
        // 1. 从告警公共标签提取 instance 与 priority
        String instance = null;
        String priority = null;
        String content = null;
        if (alert.getCommonLabels() != null) {
            instance = alert.getCommonLabels().get("instance");
            priority = alert.getCommonLabels().get("priority");
        }
        // 2. 从公共注解提取短信正文：优先 summary，其次 description，最后取首个注解值
        if (alert.getCommonAnnotations() != null) {
            content = alert.getCommonAnnotations().get("summary");
            if (content == null) {
                content = alert.getCommonAnnotations().get("description");
            }
            if (content == null) {
                content = alert.getCommonAnnotations().values().stream().findFirst().orElse(null);
            }
        }
        // 组装阿里云短信模板变量
        Map<String, String> templateParam = new HashMap<>(8);
        templateParam.put("instance", instance == null ? alert.getGroupKey() : instance);
        templateParam.put("priority", priority == null ? "unknown" : priority);
        templateParam.put("content", content);
        // 3. 调用阿里云 SendSms API 发送
        sendSms(receiver.getPhone(), JsonUtils.toJsonString(templateParam));
    }

    /**
     * 调用阿里云 SendSms API 发送短信。
     *
     * <p>执行流程：
     * <ol>
     *   <li>构建规范化查询字符串（TreeMap 保证键字典序）；</li>
     *   <li>生成时间戳与 nonce，计算 ACS3 签名得到 Authorization 头；</li>
     *   <li>构造 HTTP POST 请求并执行；</li>
     *   <li>解析响应，非 200 或 Code!=OK 时抛异常。</li>
     * </ol>
     *
     * @param phoneNumber   手机号
     * @param templateParam 模板变量 JSON 字符串
     */
    private void sendSms(String phoneNumber, String templateParam) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // 1. 构建规范化查询字符串（TreeMap 按字典序排列键）
            Map<String, String> queryParams = new TreeMap<>();
            queryParams.put("PhoneNumbers", phoneNumber);
            queryParams.put("SignName", signName);
            queryParams.put("TemplateCode", templateCode);
            queryParams.put("TemplateParam", templateParam);
            StringBuilder canonicalQueryString = new StringBuilder();
            queryParams.forEach((key, value) -> {
                if (!canonicalQueryString.isEmpty()) {
                    canonicalQueryString.append("&");
                }
                canonicalQueryString.append(percentEncode(key))
                        .append("=")
                        .append(percentEncode(value));
            });
            // 2. 生成时间戳与 nonce，用于签名与防重放
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            sdf.setTimeZone(new SimpleTimeZone(0, "GMT"));
            String timestamp = sdf.format(new Date());
            String nonce = UUID.randomUUID().toString();
            // 计算 ACS3 签名得到 Authorization 头
            String authorization = calculateAuthorization(canonicalQueryString.toString(), timestamp, nonce);
            // 3. 构造 POST 请求
            String url = "https://" + HOST + "/?" + canonicalQueryString;
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Host", HOST);
            httpPost.setHeader("Authorization", authorization);
            httpPost.setHeader("x-acs-action", ACTION);
            httpPost.setHeader("x-acs-version", API_VERSION);
            httpPost.setHeader("x-acs-date", timestamp);
            httpPost.setHeader("x-acs-signature-nonce", nonce);
            // 空请求体的 SHA256 摘要，ACS3 协议要求
            httpPost.setHeader("x-acs-content-sha256", CryptoUtils.sha256Hex(""));
            httpPost.setEntity(new StringEntity("", StandardCharsets.UTF_8));
            // 4. 执行请求并校验响应
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity());
                log.debug("Aliyun SMS response status: {}, body: {}", statusCode, responseBody);
                if (statusCode != 200) {
                    throw new AlertNoticeException("Aliyun SMS http status " + statusCode + ": " + responseBody);
                }
                // 解析响应 JSON，校验 Code 字段是否为 OK
                JsonNode resp = JsonUtils.parseTree(responseBody);
                JsonNode codeNode = resp == null ? null : resp.get("Code");
                String code = codeNode == null ? null : codeNode.asText();
                if (!"OK".equals(code)) {
                    JsonNode msgNode = resp == null ? null : resp.get("Message");
                    String message = msgNode == null ? null : msgNode.asText();
                    throw new AlertNoticeException("Aliyun SMS failed, code: " + code + ", message: " + message);
                }
                log.info("Send SMS via Aliyun success, phone: {}", phoneNumber);
            }
        } catch (AlertNoticeException e) {
            throw e;
        } catch (Exception e) {
            throw new AlertNoticeException("[Aliyun SMS Error] " + e.getMessage());
        }
    }

    /**
     * 计算 ACS3 Authorization 头。
     *
     * <p>执行流程：
     * <ol>
     *   <li>构建规范化请求（HTTP 方法 + 路径 + 查询串 + 头部 + 摘要）；</li>
     *   <li>拼接签名串：{@code 算法 + "\n" + SHA256(规范化请求)}；</li>
     *   <li>用 AccessKeySecret 对签名串做 HMAC-SHA256 得到签名；</li>
     *   <li>拼装 {@code ACS3-HMAC-SHA256 Credential=...,Signature=...} 头。</li>
     * </ol>
     *
     * @param canonicalQueryString 规范化查询字符串
     * @param timestamp            请求时间戳（x-acs-date 头）
     * @param nonce                签名随机数（x-acs-signature-nonce 头）
     * @return 完整的 Authorization 头值
     */
    private String calculateAuthorization(String canonicalQueryString, String timestamp, String nonce) {
        // 1. 构建规范化请求
        String canonicalRequest = buildCanonicalRequest(canonicalQueryString, timestamp, nonce);
        // 2. 拼接签名串：算法 + 换行 + 规范化请求的 SHA256 摘要
        String stringToSign = ALGORITHM + "\n" + CryptoUtils.sha256Hex(canonicalRequest);
        // 3. 用 AccessKeySecret 对签名串做 HMAC-SHA256 得到小写十六进制签名
        String signature = CryptoUtils.hmacSha256Hex(accessKeySecret, stringToSign);
        // 4. 拼装 Authorization 头：Credential=AccessKeyId, SignedHeaders=..., Signature=签名
        return ALGORITHM + " Credential=" + accessKeyId
                + ",SignedHeaders=host;x-acs-action;x-acs-content-sha256;x-acs-date;"
                + "x-acs-signature-nonce;x-acs-version,Signature=" + signature;
    }

    /**
     * 构建 ACS3 规范化请求字符串。
     *
     * @param canonicalQueryString 规范化查询字符串
     * @param timestamp            请求时间戳
     * @param nonce                签名随机数
     * @return 规范化请求字符串
     */
    private String buildCanonicalRequest(String canonicalQueryString, String timestamp, String nonce) {
        return "POST\n"
                + "/\n"
                + canonicalQueryString + "\n"
                + "host:" + HOST + "\n"
                + "x-acs-action:" + ACTION + "\n"
                + "x-acs-content-sha256:" + CryptoUtils.sha256Hex("") + "\n"
                + "x-acs-date:" + timestamp + "\n"
                + "x-acs-signature-nonce:" + nonce + "\n"
                + "x-acs-version:" + API_VERSION + "\n\n"
                + "host;x-acs-action;x-acs-content-sha256;x-acs-date;"
                + "x-acs-signature-nonce;x-acs-version\n"
                + CryptoUtils.sha256Hex("");
    }

    /**
     * 阿里云规范的百分号编码：URL 编码后将 {@code +} 替换为 {@code %20}、
     * {@code *} 替换为 {@code %2A}、{@code %7E} 还原为 {@code ~}。
     *
     * @param value 待编码的原始值
     * @return 编码后的字符串
     */
    private String percentEncode(String value) {
        return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8)
                .replace("+", "%20")
                .replace("*", "%2A")
                .replace("%7E", "~");
    }

    @Override
    public String getType() {
        return SmsClientFactory.TYPE_ALIBABA;
    }

    @Override
    public boolean checkConfig() {
        // 四个必填字段任一为空即视为配置不完整
        return !accessKeyId.isBlank() && !accessKeySecret.isBlank()
                && !signName.isBlank() && !templateCode.isBlank();
    }
}
