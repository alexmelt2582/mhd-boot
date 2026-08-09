package com.mhd.alert.notice.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mhd.alert.entity.AlertGroup;
import com.mhd.alert.entity.NoticeReceiver;
import com.mhd.alert.entity.NoticeTemplate;
import com.mhd.alert.notice.AbstractAlertNoticeHandlerImpl;
import com.mhd.alert.notice.AlertNoticeException;
import com.mhd.alert.notice.AlertNoticeTypeEnum;
import com.mhd.boot.common.utils.StringUtils;
import com.mhd.boot.common.utils.encrypt.CryptoUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 钉钉机器人渠道通知处理器。
 *
 * <p>通过钉钉群机器人 webhook 发送 markdown 告警消息，支持加签模式（appSecret）。
 * 加签模式下，webhook URL 需追加 {@code &timestamp=<ts>&sign=<sign>}，sign 由
 * {@link CryptoUtils#hmacSha256Base64(String, String)} 计算。
 *
 * @author zhao-hao-dong
 */
@Component
@Slf4j
public class DingTalkRobotAlertNoticeHandlerImpl extends AbstractAlertNoticeHandlerImpl {

    /**
     * 发送钉钉机器人通知。
     *
     * <p>执行流程：
     * <ol>
     *   <li>渲染模板为 markdown 并组装请求体；</li>
     *   <li>拼接 webhook URL：前缀 + accessToken；若配置 appSecret 则追加签名；</li>
     *   <li>POST 请求并校验 errcode；</li>
     *   <li>若配置了 @ 手机号或用户 ID，追加发送 text 类型 @ 消息。</li>
     * </ol>
     *
     * @param receiver       通知接收人，{@code accessToken} 为机器人 token
     * @param noticeTemplate 通知模板，渲染为 markdown 正文
     * @param alert          告警组，作为模板渲染数据来源
     * @throws AlertNoticeException HTTP 调用失败或 errcode 非 0 时抛出
     */
    @Override
    public void send(NoticeReceiver receiver, NoticeTemplate noticeTemplate, AlertGroup alert) throws AlertNoticeException {
        try {
            // 1. 渲染模板为 markdown 并组装请求体
            DingTalkWebHookDto dingTalkWebHookDto = new DingTalkWebHookDto();
            DingTalkWebHookDto.MarkdownDTO markdownDTO = new DingTalkWebHookDto.MarkdownDTO();
            markdownDTO.setText(renderContent(noticeTemplate, alert));
            markdownDTO.setTitle(NOTIFY_TITLE);
            dingTalkWebHookDto.setMarkdown(markdownDTO);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<DingTalkWebHookDto> httpEntity = new HttpEntity<>(dingTalkWebHookDto, headers);
            // 2. 拼接 webhook URL：前缀 + accessToken；若配置 appSecret 则追加加签参数
            StringBuilder webHookUrlBuilder = new StringBuilder()
                    .append(alertProperties.getDingTalkWebhookUrl())
                    .append(receiver.getAccessToken());
            if (StringUtils.isNotBlank(receiver.getAppSecret())) {
                webHookUrlBuilder.append(signSecret(receiver.getAppSecret()));
            }
            String webHookUrl = webHookUrlBuilder.toString();
            // 3. POST 请求并校验 errcode
            ResponseEntity<CommonRobotNotifyResp> responseEntity = restTemplate.postForEntity(
                    webHookUrl, httpEntity, CommonRobotNotifyResp.class);
            if (responseEntity.getStatusCode() != HttpStatus.OK || responseEntity.getBody() == null) {
                throw new AlertNoticeException("Http StatusCode " + responseEntity.getStatusCode());
            }
            if (responseEntity.getBody().getErrCode() != 0) {
                log.warn("Send DingTalk webHook: {} failed: {}", webHookUrl, responseEntity.getBody().getErrMsg());
                throw new AlertNoticeException(responseEntity.getBody().getErrMsg());
            }
            log.debug("Send DingTalk webHook: {} success", webHookUrl);
            // 4. 若配置了 @ 手机号或用户 ID，追加发送 text 类型 @ 消息
            DingTalkWebHookDto atDto = checkNeedAtNominator(receiver);
            if (atDto != null) {
                HttpEntity<DingTalkWebHookDto> atEntity = new HttpEntity<>(atDto, headers);
                restTemplate.postForEntity(webHookUrl, atEntity, CommonRobotNotifyResp.class);
                log.debug("Send DingTalk @ message webHook: {} success", webHookUrl);
            }
        } catch (AlertNoticeException e) {
            throw e;
        } catch (Exception e) {
            throw new AlertNoticeException("[DingTalk Notify Error] " + e.getMessage());
        }
    }

    /**
     * 检查是否需要 @ 指定人员，需要时返回 text 类型消息体。
     *
     * @param receiver 通知接收人，可能含 phone（@手机号）或 tgUserId（@用户 ID）
     * @return @ 消息体；receiver 无 phone 与 tgUserId 时返回 null
     */
    private DingTalkWebHookDto checkNeedAtNominator(NoticeReceiver receiver) {
        if (StringUtils.isBlank(receiver.getPhone()) && StringUtils.isBlank(receiver.getTgUserId())) {
            return null;
        }
        DingTalkWebHookDto dto = new DingTalkWebHookDto();
        dto.setMsgType(DingTalkWebHookDto.TEXT_MSG_TYPE);
        DingTalkWebHookDto.AtDTO atDTO = new DingTalkWebHookDto.AtDTO();
        if (StringUtils.isNotBlank(receiver.getPhone())) {
            atDTO.setAtMobiles(StringUtils.splitList(receiver.getPhone()));
        }
        if (StringUtils.isNotBlank(receiver.getTgUserId())) {
            atDTO.setAtUserIds(StringUtils.splitList(receiver.getTgUserId()));
        }
        dto.setAt(atDTO);
        DingTalkWebHookDto.TextDTO textDTO = new DingTalkWebHookDto.TextDTO();
        textDTO.setContent(NOTIFY_TITLE);
        dto.setText(textDTO);
        return dto;
    }

    /**
     * 计算钉钉加签模式的签名参数。
     *
     * <p>签名算法：{@code HMAC-SHA256(appSecret, timestamp + "\n" + appSecret)}，
     * 结果做 URL 编码后拼为 {@code &timestamp=<ts>&sign=<sign>}。
     *
     * @param secret 钉钉机器人 appSecret
     * @return 拼接好的 {@code &timestamp=...&sign=...} 字符串
     * @throws Exception 签名计算失败时抛出
     */
    private String signSecret(String secret) throws Exception {
        Long timestamp = System.currentTimeMillis();
        String sign = URLEncoder.encode(
                CryptoUtils.hmacSha256Base64(secret, timestamp + "\n" + secret),
                StandardCharsets.UTF_8);
        return String.format("&timestamp=%s&sign=%s", timestamp, sign);
    }

    /**
     * 返回钉钉机器人渠道类型枚举。
     *
     * @return {@link AlertNoticeTypeEnum#DING_TALK_ROBOT}
     */
    @Override
    public AlertNoticeTypeEnum type() {
        return AlertNoticeTypeEnum.DING_TALK_ROBOT;
    }

    /**
     * 钉钉机器人 webhook 请求体。
     */
    @Data
    public static class DingTalkWebHookDto {

        /**
         * 默认消息类型：markdown
         */
        private static final String DEFAULT_MSG_TYPE = "markdown";

        /**
         * text 消息类型（用于 @ 消息）
         */
        private static final String TEXT_MSG_TYPE = "text";

        /**
         * 消息类型，默认 markdown
         */
        @JsonProperty("msgtype")
        private String msgType = DEFAULT_MSG_TYPE;

        /**
         * markdown 消息体
         */
        private MarkdownDTO markdown;

        /**
         * @ 信息体
         */
        private AtDTO at;

        /**
         * text 消息体
         */
        private TextDTO text;

        /**
         * text 消息内容
         */
        @Data
        public static class TextDTO {
            /**
             * 消息正文
             */
            private String content;
        }

        /**
         * markdown 消息内容
         */
        @Data
        public static class MarkdownDTO {
            /**
             * 消息正文
             */
            private String text;

            /**
             * 消息标题
             */
            private String title;
        }

        /**
         * @ 配置
         */
        @Data
        public static class AtDTO {
            /**
             * 是否 @ 全员
             */
            private Boolean isAtAll;

            /**
             * @ 的用户 ID 列表
             */
            private List<String> atUserIds;

            /**
             * @ 的手机号列表
             */
            private List<String> atMobiles;
        }
    }
}
