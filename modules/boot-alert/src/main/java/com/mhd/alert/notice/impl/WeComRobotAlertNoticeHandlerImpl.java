package com.mhd.alert.notice.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mhd.alert.entity.AlertGroup;
import com.mhd.alert.entity.NoticeReceiver;
import com.mhd.alert.entity.NoticeTemplate;
import com.mhd.alert.notice.AbstractAlertNoticeHandlerImpl;
import com.mhd.alert.notice.AlertNoticeException;
import com.mhd.alert.notice.AlertNoticeTypeEnum;
import com.mhd.boot.common.utils.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * 企微机器人渠道通知处理器。
 *
 * <p>通过企业微信群机器人 webhook 发送 markdown 告警消息，并支持 @ 指定手机号或用户 ID。
 * webhook URL 由 {@link com.mhd.alert.config.AlertProperties#getWeWorkWebhookUrl()} 前缀
 * + {@code NoticeReceiver.wechatId} 拼接而成。
 *
 * @author zhao-hao-dong
 */
@Component
@Slf4j
public class WeComRobotAlertNoticeHandlerImpl extends AbstractAlertNoticeHandlerImpl {

    /**
     * 发送企微机器人通知。
     *
     * <p>执行流程：
     * <ol>
     *   <li>校验 wechatId 合法性（非空且仅含字母数字下划线短横线）；</li>
     *   <li>渲染模板为 markdown 内容并组装请求体；</li>
     *   <li>POST 到 webhook URL，校验 errcode；</li>
     *   <li>若配置了 @ 手机号或用户 ID，追加发送 text 类型 @ 消息。</li>
     * </ol>
     *
     * @param receiver       通知接收人，{@code wechatId} 为机器人 key
     * @param noticeTemplate 通知模板，渲染为 markdown 正文
     * @param alert          告警组，作为模板渲染数据来源
     * @throws AlertNoticeException wechatId 非法或 HTTP 调用失败时抛出
     */
    @Override
    public void send(NoticeReceiver receiver, NoticeTemplate noticeTemplate, AlertGroup alert) throws AlertNoticeException {
        try {
            // 1. 校验 wechatId 合法性，防止 URL 注入
            String wechatId = receiver.getWechatId();
            if (!isValidWechatId(wechatId)) {
                log.warn("Invalid WeChat ID: {}", wechatId);
                throw new AlertNoticeException("Invalid WeChat ID provided.");
            }
            // 2. 渲染模板为 markdown 并组装请求体
            WeWorkWebHookDto weWorkWebHookDTO = new WeWorkWebHookDto();
            WeWorkWebHookDto.MarkdownDTO markdownDTO = new WeWorkWebHookDto.MarkdownDTO();
            markdownDTO.setContent(renderContent(noticeTemplate, alert));
            weWorkWebHookDTO.setMarkdown(markdownDTO);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<WeWorkWebHookDto> httpEntity = new HttpEntity<>(weWorkWebHookDTO, headers);
            // 3. POST 到 webhook URL 并校验 errcode
            String webHookUrl = alertProperties.getWeWorkWebhookUrl() + wechatId;
            ResponseEntity<CommonRobotNotifyResp> entity = restTemplate.postForEntity(
                    webHookUrl, httpEntity, CommonRobotNotifyResp.class);
            if (entity.getStatusCode() != HttpStatus.OK || entity.getBody() == null) {
                throw new AlertNoticeException("Http StatusCode " + entity.getStatusCode());
            }
            if (entity.getBody().getErrCode() != 0) {
                log.warn("Send WeWork webHook: {} failed: {}", webHookUrl, entity.getBody().getErrMsg());
                throw new AlertNoticeException(entity.getBody().getErrMsg());
            }
            log.debug("Send WeWork webHook: {} success", webHookUrl);
            // 4. 若配置了 @ 手机号或用户 ID，追加发送 text 类型 @ 消息
            WeWorkWebHookDto atDto = checkNeedAtNominator(receiver);
            if (atDto != null) {
                HttpEntity<WeWorkWebHookDto> atEntity = new HttpEntity<>(atDto, headers);
                restTemplate.postForEntity(webHookUrl, atEntity, CommonRobotNotifyResp.class);
                log.debug("Send WeWork @ message webHook: {} success", webHookUrl);
            }
        } catch (AlertNoticeException e) {
            throw e;
        } catch (Exception e) {
            throw new AlertNoticeException("[WeWork Notify Error] " + e.getMessage());
        }
    }

    /**
     * 检查是否需要 @ 指定人员，需要时返回 text 类型消息体。
     *
     * @param receiver 通知接收人，可能含 phone（@手机号）或 userId（@用户 ID）
     * @return @ 消息体；receiver 无 phone 与 userId 时返回 null
     */
    private WeWorkWebHookDto checkNeedAtNominator(NoticeReceiver receiver) {
        // phone 与 userId 均为空时不发送 @ 消息
        if (StringUtils.isBlank(receiver.getPhone()) && StringUtils.isBlank(receiver.getUserId())) {
            return null;
        }
        WeWorkWebHookDto dto = new WeWorkWebHookDto();
        dto.setMsgtype(WeWorkWebHookDto.TEXT_MSG_TYPE);
        WeWorkWebHookDto.TextDTO textDto = new WeWorkWebHookDto.TextDTO();
        // @ 手机号列表
        if (StringUtils.isNotBlank(receiver.getPhone())) {
            textDto.setMentionedMobileList(StringUtils.splitList(receiver.getPhone()));
        }
        // @ 用户 ID 列表
        if (StringUtils.isNotBlank(receiver.getUserId())) {
            textDto.setMentionedList(StringUtils.splitList(receiver.getUserId()));
        }
        dto.setText(textDto);
        return dto;
    }

    /**
     * 校验 wechatId 格式：非空且仅含字母、数字、下划线、短横线。
     *
     * @param wechatId 待校验的机器人 key
     * @return true 表示合法
     */
    private boolean isValidWechatId(String wechatId) {
        return StringUtils.isNotBlank(wechatId) && wechatId.matches("^[a-zA-Z0-9_-]+$");
    }

    /**
     * 返回企微机器人渠道类型枚举。
     *
     * @return {@link AlertNoticeTypeEnum#WE_COM_ROBOT}
     */
    @Override
    public AlertNoticeTypeEnum type() {
        return AlertNoticeTypeEnum.WE_COM_ROBOT;
    }

    /**
     * 企微机器人 webhook 请求体。
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WeWorkWebHookDto {

        /**
         * 默认消息类型：markdown
         */
        public static final String DEFAULT_MSG_TYPE = "markdown";

        /**
         * text 消息类型（用于 @ 消息）
         */
        public static final String TEXT_MSG_TYPE = "text";

        /**
         * 消息类型，默认 markdown
         */
        @Builder.Default
        @JsonProperty("msgtype")
        private String msgtype = DEFAULT_MSG_TYPE;

        /**
         * markdown 消息体
         */
        private MarkdownDTO markdown;

        /**
         * text 消息体（用于 @）
         */
        private TextDTO text;

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

        /**
         * text 消息内容与 @ 列表
         */
        @Data
        public static class TextDTO {
            /**
             * 消息正文
             */
            private String content;

            /**
             * @ 的用户 ID 列表
             */
            @JsonProperty("mentioned_list")
            private List<String> mentionedList;

            /**
             * @ 的手机号列表
             */
            @JsonProperty("mentioned_mobile_list")
            private List<String> mentionedMobileList;
        }
    }
}
