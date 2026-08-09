package com.mhd.alert.notice.impl.sms;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 短信服务配置属性。
 *
 * <p>对应配置前缀 {@code com.mhd.alert.sms}，在 {@code application.yml} 中按服务商配置：
 *
 * <pre>{@code
 * com:
 *   mhd:
 *     alert:
 *       sms:
 *         enable: true
 *         type: alibaba          # alibaba | tencent | http
 *         alibaba:
 *           access-key-id: LTAI****
 *           access-key-secret: ****
 *           sign-name: 监控告警
 *           template-code: SMS_****
 *         http:                   # 通用 HTTP 短信网关
 *           url: https://sms.example.com/send
 *           token: ****
 * }</pre>
 *
 * <p>{@link #type} 决定 {@link SmsClientFactory} 实例化哪个 {@link SmsClient} 实现；
 * {@link #enable} 为 false 时工厂返回 null，{@code SmsAlertNoticeHandlerImpl} 会抛出
 * 异常提示「短信服务未启用」。
 *
 * @author zhao-hao-dong
 */
@Data
@Component
@ConfigurationProperties(prefix = "com.mhd.alert.sms")
public class SmsProperties {

    /**
     * 是否启用短信服务，默认 false。未启用时工厂返回 null 客户端。
     */
    private boolean enable = false;

    /**
     * 短信服务商类型：{@code alibaba}（阿里云）/ {@code http}（通用 HTTP 网关）等。
     */
    private String type;

    /**
     * 阿里云短信配置
     */
    private AlibabaProperties alibaba = new AlibabaProperties();

    /**
     * 通用 HTTP 短信网关配置（支持任意提供 HTTP API 的短信服务商）
     */
    private HttpProperties http = new HttpProperties();

    /**
     * 阿里云短信服务配置。
     *
     * <p>对应阿里云 dysmsapi 服务，参考
     * <a href="https://next.api.aliyun.com/document/Dysmsapi/2017-05-25/SendSms">SendSms API</a>。
     *
     * @author zhao-hao-dong
     */
    @Data
    public static class AlibabaProperties {

        /**
         * 阿里云 AccessKey ID
         */
        private String accessKeyId;

        /**
         * 阿里云 AccessKey Secret
         */
        private String accessKeySecret;

        /**
         * 短信签名，需在阿里云控制台提前申请并通过审核
         */
        private String signName;

        /**
         * 短信模板 Code，需在阿里云控制台提前创建并通过审核
         */
        private String templateCode;
    }

    /**
     * 通用 HTTP 短信网关配置。
     *
     * <p>适用于自建或第三方短信网关，通过 HTTP POST 提交手机号与内容即可发送。
     * 请求体为 JSON：{@code {"phone": "...", "content": "..."}}，Header 携带
     * {@code Authorization: Bearer <token>} 鉴权。
     *
     * @author zhao-hao-dong
     */
    @Data
    public static class HttpProperties {

        /**
         * 短信网关请求 URL
         */
        private String url;

        /**
         * 短信网关鉴权 Token，以 Bearer 方式携带在 Authorization Header
         */
        private String token;
    }
}
