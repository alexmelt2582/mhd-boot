package com.mhd.alert.extern.impl;

import cn.hutool.core.date.DateUtil;
import com.mhd.alert.entity.AlertEvent;
import com.mhd.alert.enums.AlertStatusEnum;
import com.mhd.alert.extern.ExternAlertService;
import com.mhd.alert.extern.dto.HuaweiCloudExternAlert;
import com.mhd.alert.reduce.AlarmCommonReduce;
import com.mhd.boot.common.utils.date.DateUtils;
import com.mhd.boot.common.utils.json.JsonUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;

import static com.mhd.alert.extern.dto.HuaweiCloudExternAlert.*;
import static com.mhd.alert.extern.dto.HuaweiCloudExternAlert.AlertType.*;

/**
 *
 *
 * @author zhao-hao-dong
 **/
@RequiredArgsConstructor
@Slf4j
@Service
public class HuaweiCloudExternAlertService implements ExternAlertService {
    private static final String CERTIFICATE_TYPE = "X.509";
    private static final String CHARSET_UTF8 = StandardCharsets.UTF_8.name();
    private static final String SUBSCRIBE_URL_PREFIX = "https://console.huaweicloud.com/smn/subscription/confirm";

    private final AlarmCommonReduce alarmCommonReduce;


    @Override
    public void addExternAlert(String content) {
        HuaweiCloudExternAlert externAlert = JsonUtils.parseObject(content, HuaweiCloudExternAlert.class);
        if (externAlert == null || StringUtils.isBlank(externAlert.getMessage())) {
            log.warn("Failure to parse external alert content. content: {}", content);
            return;
        }
        if (!isMessageValid(externAlert)) {
            log.warn("Huawei cloud alert verify failed. content: {}", content);
            return;
        }
        process(externAlert);
    }
    private void process(HuaweiCloudExternAlert externAlert) {
        if (NOTIFICATION.getType().equals(externAlert.getType())) {
            Optional.ofNullable(buildSendAlert(externAlert)).ifPresent(alarmCommonReduce::reduceAndSendAlarm);
        } else if (SUBSCRIPTION.getType().equals(externAlert.getType())) {
            autoSubscribeForUrl(externAlert.getSubscribeUrl());
        } else if (UNSUBSCRIBE.getType().equals(externAlert.getType())) {
            log.warn("Huawei cloud notifies the recipient of the notification to cancel the subscription.");
        }
    }

    private AlertEvent buildSendAlert(HuaweiCloudExternAlert externAlert) {
        HuaweiCloudExternAlert.AlertMessage message = JsonUtils.parseObject(externAlert.getMessage(), HuaweiCloudExternAlert.AlertMessage.class);
        if (null == message || null == message.getData()) {
            log.warn("Failure to parse external alert message. message: {}", externAlert.getMessage());
            return null;
        }
        boolean isAlarm = null != message.getData().getAlarm() && message.getData().getAlarm();
        Long alarmTime = DateUtils.strToDate(message.getData().getAlarmTime(), "yyyy/MM/dd HH:mm:ss 'GMT'XXX").toInstant(ZoneOffset.UTC).toEpochMilli();
        return AlertEvent.builder()
                .triggerTimes(1)
                .status(isAlarm ? AlertStatusEnum.FIRING.getCode() : AlertStatusEnum.RESOLVED.getCode())
                .startAt(alarmTime)
                .activeAt(Instant.now().toEpochMilli())
                .endAt(isAlarm ? null : alarmTime)
                .labels(buildLabels(message.getData()))
                .annotations(buildAnnotations(message.getData()))
                .content(formatContent(externAlert.getSubject(), message.getData()))
                .build();
    }

    /**
     * Build basic annotations and fill annotations for huawei cloud.
     *
     * @param alertData alert content entity
     * @return annotations
     */
    private Map<String, String> buildAnnotations(HuaweiCloudExternAlert.AlertData alertData) {
        Map<String, String> annotations = new HashMap<>(8);
        if (null != alertData) {
            putIfNotBlank(annotations, "region", alertData.getRegion());
            putIfNotBlank(annotations, "dimensionName", alertData.getDimensionName());
            putIfNotBlank(annotations, "resourceName", alertData.getResourceName());
            putIfNotBlank(annotations, "alarmRecordId", alertData.getAlarmRecordId());
        }
        return annotations;
    }

    /**
     * Build basic labels and fill labels for huawei cloud.
     *
     * @param alertData alert content entity
     * @return labels
     */
    private Map<String, String> buildLabels(HuaweiCloudExternAlert.AlertData alertData) {
        Map<String, String> labels = new HashMap<>(8);
        labels.put("__source__", "huaweicloud-ces");
        if (null != alertData) {
            putIfNotBlank(labels, "namespace", alertData.getNamespace());
            putIfNotBlank(labels, "metricName", alertData.getMetricName());
            putIfNotBlank(labels, "resourceId", alertData.getResourceId());
            putIfNotBlank(labels, "level", alertData.getAlarmLevel());
        }
        return labels;
    }

    /**
     * todo i18n
     *
     * @param subject alert subject
     * @param alertData alert content entity
     * @return content
     */
    private String formatContent(String subject, HuaweiCloudExternAlert.AlertData alertData) {
        if (null == alertData) {
            return subject;
        }
        return MessageFormat.format(
                "{0} threshold:{1}{2}, current：{3}",
                subject,
                alertData.getComparisonOperator(),
                alertData.getValue(),
                alertData.getCurrentData()
        );
    }

    /**
     * Automatic subscription url.
     *
     * @param subscribeUrl subscribeUrl
     */
    public void autoSubscribeForUrl(String subscribeUrl) {
        if (StringUtils.isBlank(subscribeUrl)) {
            return;
        }
        if (!subscribeUrl.startsWith(SUBSCRIBE_URL_PREFIX)) {
            throw new SecurityException("Untrusted domain: " + subscribeUrl);
        }
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(subscribeUrl);
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity());

                if (statusCode != 200) {
                    log.error("Subscribe url request failed with status code: " + statusCode + ", response: " + responseBody);
                    return;
                }
                JsonNode jsonResponse = JsonUtils.parseTree(responseBody);
                if (jsonResponse == null) {
                    throw new RuntimeException("Subscribe url failed with status code: " + statusCode + ", response: " + responseBody);
                }
                JsonNode surnNode = jsonResponse.get("subscription_urn");
                if (surnNode == null || StringUtils.isBlank(surnNode.asText())) {
                    throw new RuntimeException("Subscribe url failed with status code: " + statusCode + ", response: " + responseBody);
                }
                log.info("Successfully subscribed to Huawei Cloud(SMN) url.");
            }
        } catch (Exception e) {
            log.error("Failed to subscribe url request: {}", e.getMessage());
        }
    }

    /**
     * Verifying the signature of huawei cloud alert message.
     *
     * @param externAlert alert content entity
     * @return verification result
     * @throws SecurityException thrown when validation fails
     */
    private boolean isMessageValid(HuaweiCloudExternAlert externAlert) {
        try {
            String signMessage = buildSignMessage(externAlert);
            if (StringUtils.isBlank(signMessage)) {
                throw new SecurityException("Verify sign message is null");
            }
            X509Certificate cert = getCertificate(externAlert.getSigningCertUrl());
            return verifySignature(signMessage, cert, externAlert.getSignature());
        } catch (Exception e) {
            log.error("Failed to verify message signature: ", e);
            return false;
        }
    }

    /**
     * Build sign message.
     *
     * @param externAlert alert content entity
     * @return sign message
     */
    private String buildSignMessage(HuaweiCloudExternAlert externAlert) {
        if (NOTIFICATION.getType().equals(externAlert.getType())) {
            return buildNotificationMessage(externAlert);
        } else if (SUBSCRIPTION.getType().equals(externAlert.getType()) || UNSUBSCRIBE.getType().equals(externAlert.getType())){
            return buildSubscriptionMessage(externAlert);
        }
        return null;
    }

    /**
     * Building sign message of 'Notification' type
     *
     * @param externAlert alert content entity
     * @return sign message
     */
    private String buildNotificationMessage(HuaweiCloudExternAlert externAlert) {
        StringBuilder message = new StringBuilder();
        appendField(message, FIELD_MESSAGE, externAlert.getMessage());
        appendField(message, FIELD_MESSAGE_ID, externAlert.getMessageId());
        if (StringUtils.isNotBlank(externAlert.getSubject())) {
            appendField(message, FIELD_SUBJECT, externAlert.getSubject());
        }
        appendField(message, FIELD_TIMESTAMP, externAlert.getTimestamp());
        appendField(message, FIELD_TOPIC_URN, externAlert.getTopicUrn());
        appendField(message, FIELD_TYPE, externAlert.getType());
        return message.toString();
    }

    /**
     * Building sign message of 'SubscriptionConfirmation' or 'UnsubscribeConfirmation' type
     *
     * @param externAlert alert content entity
     * @return sign message
     */
    private String buildSubscriptionMessage(HuaweiCloudExternAlert externAlert) {
        StringBuilder message = new StringBuilder();
        appendField(message, FIELD_MESSAGE, externAlert.getMessage());
        appendField(message, FIELD_MESSAGE_ID, externAlert.getMessageId());
        appendField(message, FIELD_SUBSCRIBE_URL, externAlert.getSubscribeUrl());
        appendField(message, FIELD_TIMESTAMP, externAlert.getTimestamp());
        appendField(message, FIELD_TOPIC_URN, externAlert.getTopicUrn());
        appendField(message, FIELD_TYPE, externAlert.getType());
        return message.toString();
    }

    /**
     * Obtain certificate
     *
     * @param signCertUrl sign cert url
     * @return X509 certificate
     * @throws Exception Thrown when certificate acquisition fails
     */
    private X509Certificate getCertificate(String signCertUrl) throws Exception {
        URL url = new URL(signCertUrl);
        if (!"https".equalsIgnoreCase(url.getProtocol())) {
            throw new SecurityException("Only HTTPS is allowed");
        }
        boolean trusted = Arrays.stream(Region.values()).anyMatch(ep -> ep.getEndpoint().equals(url.getHost()));
        if (!trusted) {
            throw new SecurityException("Untrusted domain: " + url.getHost());
        }
        try (InputStream in = url.openStream()) {
            CertificateFactory cf = CertificateFactory.getInstance(CERTIFICATE_TYPE);
            return (X509Certificate) cf.generateCertificate(in);
        }
    }

    /**
     * Verify signature
     *
     * @param message sign message
     * @param cert cert
     * @param signature signature
     * @return verification result
     * @throws Exception thrown when an error occurs in the validation process
     */
    private boolean verifySignature(String message, X509Certificate cert, String signature) throws Exception {
        Signature sig = Signature.getInstance(cert.getSigAlgName());
        sig.initVerify(cert.getPublicKey());
        sig.update(message.getBytes(CHARSET_UTF8));
        return sig.verify(Base64.getDecoder().decode(signature));
    }

    private void putIfNotBlank(Map<String, String> map, String key, String value) {
        if (StringUtils.isNotBlank(value)){
            map.put(key, value);
        }
    }

    private void appendField(StringBuilder builder, String fieldName, String value) {
        builder.append(fieldName).append("\n").append(value).append("\n");
    }

    @Override
    public String supportSource() {
        return "huaweicloud-ces";
    }


    /**
     * doc: <a href="https://console.huaweicloud.com/apiexplorer/#/endpoint/SMN">SMN API</a>
     */
    @Getter
    private enum Region {

        AE_AD_1("ae-ad-1", "smn.ae-ad-1.myhuaweicloud.com"),
        AF_SOUTH_1("af-south-1", "smn.af-south-1.myhuaweicloud.com"),
        AP_SOUTHEAST_1("ap-southeast-1", "smn.ap-southeast-1.myhuaweicloud.com"),
        AP_SOUTHEAST_2("ap-southeast-2", "smn.ap-southeast-2.myhuaweicloud.com"),
        AP_SOUTHEAST_3("ap-southeast-3", "smn.ap-southeast-3.myhuaweicloud.com"),
        AP_SOUTHEAST_4("ap-southeast-4", "smn.ap-southeast-4.myhuaweicloud.com"),
        CN_EAST_2("cn-east-2", "smn.cn-east-2.myhuaweicloud.com"),
        CN_EAST_3("cn-east-3", "smn.cn-east-3.myhuaweicloud.com"),
        CN_EAST_4("cn-east-4", "smn.cn-east-4.myhuaweicloud.com"),
        CN_EAST_5("cn-east-5", "smn.cn-east-5.myhuaweicloud.com"),
        CN_NORTH_1("cn-north-1", "smn.cn-north-1.myhuaweicloud.com"),
        CN_NORTH_11("cn-north-11", "smn.cn-north-11.myhuaweicloud.com"),
        CN_NORTH_12("cn-north-12", "smn.cn-north-12.myhuaweicloud.com"),
        CN_NORTH_2("cn-north-2", "smn.cn-north-2.myhuaweicloud.cn"),
        CN_NORTH_4("cn-north-4", "smn.cn-north-4.myhuaweicloud.com"),
        CN_NORTH_9("cn-north-9", "smn.cn-north-9.myhuaweicloud.com"),
        CN_SOUTH_1("cn-south-1", "smn.cn-south-1.myhuaweicloud.com"),
        CN_SOUTH_2("cn-south-2", "smn.cn-south-2.myhuaweicloud.com"),
        CN_SOUTH_4("cn-south-4", "smn.cn-south-4.myhuaweicloud.com"),
        CN_SOUTHWEST_2("cn-southwest-2", "smn.cn-southwest-2.myhuaweicloud.com"),
        CN_SOUTHWEST_3("cn-southwest-3", "smn.cn-southwest-3.myhuaweicloud.com"),
        EU_WEST_0("eu-west-0", "smn.eu-west-0.myhuaweicloud.com"),
        LA_NORTH_2("la-north-2", "smn.la-north-2.myhuaweicloud.com"),
        LA_SOUTH_2("la-south-2", "smn.la-south-2.myhuaweicloud.com"),
        MY_KUALALUMPUR_1("my-kualalumpur-1", "smn.my-kualalumpur-1.myhuaweicloud.com"),
        NA_MEXICO_1("na-mexico-1", "smn.na-mexico-1.myhuaweicloud.com"),
        RU_MOSCOW_1("ru-moscow-1", "smn.ru-moscow-1.myhuaweicloud.com"),
        SA_BRAZIL_1("sa-brazil-1", "smn.sa-brazil-1.myhuaweicloud.com"),
        TR_WEST_1("tr-west-1", "smn.tr-west-1.myhuaweicloud.com"),
        EU_WEST_101("eu-west-101", "smn.eu-west-101.myhuaweicloud.eu");

        private final String id;

        private final String endpoint;

        Region(String id, String endpoint) {
            this.id = id;
            this.endpoint = endpoint;
        }
    }
}
