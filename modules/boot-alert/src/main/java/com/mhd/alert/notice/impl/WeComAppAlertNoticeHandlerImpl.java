package com.mhd.alert.notice.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mhd.alert.entity.AlertGroup;
import com.mhd.alert.entity.NoticeReceiver;
import com.mhd.alert.entity.NoticeTemplate;
import com.mhd.alert.notice.AbstractAlertNoticeHandlerImpl;
import com.mhd.alert.notice.AlertNoticeException;
import com.mhd.alert.notice.AlertNoticeTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 企微应用消息渠道通知处理器。
 *
 * <p>通过企业微信「应用消息」接口发送 markdown 告警消息。与企微机器人不同，
 * 应用消息需先用 corpId + appSecret 换取 access_token，再调用消息发送接口，
 * 且支持按用户/部门/标签维度定向推送。
 *
 * @author zhao-hao-dong
 */
@Component
@Slf4j
public class WeComAppAlertNoticeHandlerImpl extends AbstractAlertNoticeHandlerImpl {

    /**
     * 发送应用消息的接口 URL 模板
     */
    private static final String APP_MESSAGE_URL = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=%s";

    /**
     * 获取 access_token 的接口 URL 模板
     */
    private static final String SECRET_URL = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=%s&corpsecret=%s";

    /**
     * 未指定接收人时的默认值：@all 全员
     */
    private static final String DEFAULT_ALL = "@all";

    /**
     * 发送企微应用消息通知。
     *
     * <p>执行流程：
     * <ol>
     *   <li>用 corpId + appSecret 换取 access_token；</li>
     *   <li>渲染模板为 markdown 并组装请求体，按 userId/partyId/tagId 定向推送（均空时 @all）；</li>
     *   <li>POST 到消息发送接口并校验 errcode。</li>
     * </ol>
     *
     * @param receiver       通知接收人，含 corpId/appSecret/agentId 及定向维度
     * @param noticeTemplate 通知模板，渲染为 markdown 正文
     * @param alert          告警组，作为模板渲染数据来源
     * @throws AlertNoticeException 换 token 或发送失败时抛出
     */
    @Override
    public void send(NoticeReceiver receiver, NoticeTemplate noticeTemplate, AlertGroup alert) throws AlertNoticeException {
        try {
            // 1. 换取 access_token
            ResponseEntity<WeChatAppReq> tokenResp = restTemplate.getForEntity(
                    String.format(SECRET_URL, receiver.getCorpId(), receiver.getAppSecret()), WeChatAppReq.class);
            if (tokenResp.getBody() == null) {
                throw new AlertNoticeException("Failed to get WeCom access_token: empty response");
            }
            String accessToken = tokenResp.getBody().getAccessToken();
            // 2. 渲染模板并组装请求体，按 userId/partyId/tagId 定向推送
            WeChatAppDTO.MarkdownDTO markdown = new WeChatAppDTO.MarkdownDTO();
            markdown.setContent(renderContent(noticeTemplate, alert));
            WeChatAppDTO.WeChatAppDTOBuilder builder = WeChatAppDTO.builder()
                    .msgType(WeChatAppDTO.MARKDOWN)
                    .markdown(markdown)
                    .agentId(receiver.getAgentId());
            boolean hasUserId = receiver.getUserId() != null;
            boolean hasPartyId = receiver.getPartyId() != null;
            boolean hasTagId = receiver.getTagId() != null;
            if (hasUserId) {
                builder.toUser(receiver.getUserId());
            }
            if (hasPartyId) {
                builder.toParty(receiver.getPartyId());
            }
            if (hasTagId) {
                builder.toTag(receiver.getTagId());
            }
            // 三者均未配置时 @all 全员推送
            if (!hasUserId && !hasPartyId && !hasTagId) {
                builder.toUser(DEFAULT_ALL);
            }
            WeChatAppDTO weChatAppDTO = builder.build();
            // 3. POST 到消息发送接口并校验 errcode
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<WeChatAppDTO> weChatAppEntity = new HttpEntity<>(weChatAppDTO, headers);
            ResponseEntity<WeChatAppReq> response = restTemplate.postForEntity(
                    String.format(APP_MESSAGE_URL, accessToken), weChatAppEntity, WeChatAppReq.class);
            if (response.getBody() == null || !Objects.equals(response.getBody().getErrCode(), 0)) {
                log.warn("Send Enterprise WeChat App failed: {}", response.getBody());
                throw new AlertNoticeException("Http StatusCode " + response.getStatusCode()
                        + " Error: " + (response.getBody() == null ? "empty" : response.getBody().getErrMsg()));
            }
            log.debug("Send Enterprise WeChat App success");
        } catch (AlertNoticeException e) {
            throw e;
        } catch (Exception e) {
            throw new AlertNoticeException("[Enterprise WeChat Notify Error] " + e.getMessage());
        }
    }

    /**
     * 返回企微应用消息渠道类型枚举。
     *
     * @return {@link AlertNoticeTypeEnum#WE_COM_APP}
     */
    @Override
    public AlertNoticeTypeEnum type() {
        return AlertNoticeTypeEnum.WE_COM_APP;
    }

    /**
     * 企微应用消息响应/换 token 响应体。
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WeChatAppReq {
        /**
         * 错误码，0 表示成功
         */
        @JsonProperty("errcode")
        private Integer errCode;

        /**
         * 错误信息
         */
        @JsonProperty("errmsg")
        private String errMsg;

        /**
         * access_token（换 token 接口返回）
         */
        @JsonProperty("access_token")
        private String accessToken;
    }

    /**
     * 企微应用消息请求体。
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WeChatAppDTO {

        /**
         * markdown 消息类型
         */
        public static final String MARKDOWN = "markdown";

        /**
         * 接收人用户 ID
         */
        @JsonProperty("touser")
        private String toUser;

        /**
         * 接收部门 ID
         */
        @JsonProperty("toparty")
        private String toParty;

        /**
         * 接收标签 ID
         */
        @JsonProperty("totag")
        private String toTag;

        /**
         * 消息类型
         */
        @JsonProperty("msgtype")
        private String msgType;

        /**
         * 应用 agentId
         */
        @JsonProperty("agentid")
        private Integer agentId;

        /**
         * markdown 消息体
         */
        private MarkdownDTO markdown;

        /**
         * markdown 消息内容
         */
        @Data
        public static class MarkdownDTO {
            /**
             * 消息正文
             */
            private String content;
        }
    }
}
