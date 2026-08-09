package com.mhd.alert.notice.impl;

import com.mhd.alert.entity.AlertGroup;
import com.mhd.alert.entity.NoticeReceiver;
import com.mhd.alert.entity.NoticeTemplate;
import com.mhd.alert.notice.AbstractAlertNoticeHandlerImpl;
import com.mhd.alert.notice.AlertNoticeException;
import com.mhd.alert.notice.AlertNoticeTypeEnum;
import com.mhd.boot.common.utils.StringUtils;
import com.mhd.boot.common.utils.json.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 飞书机器人渠道通知处理器。
 *
 * <p>通过飞书群机器人 webhook 发送 interactive 卡片消息。卡片支持 markdown 正文、
 * 控制台跳转按钮，并按告警优先级着色标题（红/黄/橙）。
 *
 * @author zhao-hao-dong
 */
@Component
@Slf4j
public class FlyBookRobotAlertNoticeHandlerImpl extends AbstractAlertNoticeHandlerImpl {

    /**
     * 告警优先级对应的卡片标题颜色：critical=红、warning=黄、其他=橙
     */
    private static final String[] TITLE_COLOR = {"red", "yellow", "orange"};

    /**
     * 发送飞书机器人通知。
     *
     * <p>执行流程：
     * <ol>
     *   <li>渲染模板为通知正文，并对正文做 JSON 字符串化（避免转义破坏 JSON 结构）；</li>
     *   <li>构造飞书 interactive 卡片 JSON：含 markdown 正文、控制台按钮、着色标题；</li>
     *   <li>POST 到 webhook URL（前缀 + accessToken）并校验响应 code。</li>
     * </ol>
     *
     * @param receiver       通知接收人，{@code accessToken} 为机器人 token，{@code userId} 用于 @
     * @param noticeTemplate 通知模板，渲染为卡片 markdown 正文
     * @param alert          告警组，作为模板渲染数据来源
     * @throws AlertNoticeException HTTP 调用失败或响应 code 非 0 时抛出
     */
    @Override
    public void send(NoticeReceiver receiver, NoticeTemplate noticeTemplate, AlertGroup alert) throws AlertNoticeException {
        try {
            // 1. 渲染模板为正文并 JSON 字符串化，避免内部引号破坏外层 JSON
            String notificationContent = JsonUtils.toJsonString(renderContent(noticeTemplate, alert));
            // 2. 构造飞书 interactive 卡片 JSON
            String cardMessage = createLarkMessage(receiver.getUserId(), notificationContent, (byte) 1);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> flyEntity = new HttpEntity<>(cardMessage, headers);
            // 3. POST 到 webhook URL 并校验响应 code
            String webHookUrl = alertProperties.getFlyBookWebhookUrl() + receiver.getAccessToken();
            ResponseEntity<CommonRobotNotifyResp> entity = restTemplate.postForEntity(
                    webHookUrl, flyEntity, CommonRobotNotifyResp.class);
            if (entity.getStatusCode() != HttpStatus.OK || entity.getBody() == null) {
                throw new AlertNoticeException("Http StatusCode " + entity.getStatusCode());
            }
            // 飞书响应 code 为 null 或 0 均视为成功（部分版本返回 code 字段缺失）
            if (entity.getBody().getCode() != null && entity.getBody().getCode() != 0) {
                log.warn("Send FeiShu webHook: {} failed: {}", webHookUrl, entity.getBody().getMsg());
                throw new AlertNoticeException(entity.getBody().getMsg());
            }
            log.debug("Send FeiShu webHook: {} success", webHookUrl);
        } catch (AlertNoticeException e) {
            throw e;
        } catch (Exception e) {
            throw new AlertNoticeException("[FeiShu Notify Error] " + e.getMessage());
        }
    }

    /**
     * 构造飞书 interactive 卡片消息 JSON。
     *
     * <p>卡片结构：markdown 正文 + 分隔线 + 控制台跳转按钮，标题按优先级着色。
     * 若配置了 userId，则在正文末尾追加 {@code <at id=...></at>} 标签实现 @。
     *
     * @param userId              @ 的用户 ID（逗号分隔），为空则不 @
     * @param notificationContent JSON 字符串化后的正文
     * @param priority            告警优先级索引（0=critical 红、1=warning 黄、2=其他 橙）
     * @return 完整的卡片消息 JSON 字符串
     */
    private String createLarkMessage(String userId, String notificationContent, byte priority) {
        // 飞书卡片模板：使用 %s 占位符依次填入正文、控制台 URL、标题颜色
        String larkCardMessage = """
                {
                "msg_type": "interactive",
                "card":  {
                     "schema": "2.0",
                     "config": {
                         "update_multi": true,
                         "locales": ["en_us", "zh_cn"],
                         "style": {"text_size": {"normal_v2": {"default": "normal", "pc": "normal", "mobile": "heading"}}}
                     },
                     "body": {
                         "direction": "vertical",
                         "padding": "12px 12px 12px 12px",
                         "elements": [
                             {"tag": "markdown", "content": "%s", "i18n_content": {"en_us": ""}, "text_align": "left", "text_size": "normal_v2", "margin": "0px 0px 0px 0px"},
                             {"tag": "hr", "margin": "0px 0px 0px 0px"},
                             {"tag": "column_set", "horizontal_align": "left", "columns": [
                                 {"tag": "column", "width": "weighted", "elements": [
                                     {"tag": "button", "text": {"tag": "plain_text", "content": "登入控制台", "i18n_content": {"en_us": "Login In"}},
                                      "type": "default", "width": "default", "size": "medium",
                                      "behaviors": [{"type": "open_url", "default_url": "%s", "pc_url": "", "ios_url": "", "android_url": ""}]}
                                 ], "direction": "horizontal", "vertical_spacing": "8px", "horizontal_align": "left", "vertical_align": "top", "weight": 1}
                             ], "margin": "0px 0px 0px 0px"}
                         ]
                     },
                     "header": {
                         "title": {"tag": "plain_text", "content": "告警通知", "i18n_content": {"en_us": "Alarm Notification"}},
                         "subtitle": {"tag": "plain_text", "content": ""},
                         "template": "%s",
                         "padding": "12px 12px 12px 12px"
                     }
                 }
                }
                """;
        // 拼接 @ 用户标签：飞书格式为 <at id=xxx></at>
        String atUserElement = "";
        if (StringUtils.isNotBlank(userId)) {
            atUserElement = "\\n" + Arrays.stream(userId.split(","))
                    .map(id -> "<at id=" + id + "></at>")
                    .collect(Collectors.joining(" "));
        }
        // 去除 JSON 字符串化外层的引号，便于嵌入卡片模板
        if (notificationContent.startsWith("\"") && notificationContent.endsWith("\"")) {
            notificationContent = StringUtils.removeStart(notificationContent, "\"");
            notificationContent = StringUtils.removeEnd(notificationContent, "\"");
        }
        return String.format(larkCardMessage,
                notificationContent.replace("\"", "\\\"") + atUserElement,
                alertProperties.getConsoleUrl(), TITLE_COLOR[priority]);
    }

    /**
     * 返回飞书机器人渠道类型枚举。
     *
     * @return {@link AlertNoticeTypeEnum#FLY_BOOK_ROBOT}
     */
    @Override
    public AlertNoticeTypeEnum type() {
        return AlertNoticeTypeEnum.FLY_BOOK_ROBOT;
    }
}
