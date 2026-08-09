package com.mhd.alert.notice.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mhd.alert.entity.AlertGroup;
import com.mhd.alert.entity.NoticeReceiver;
import com.mhd.alert.entity.NoticeTemplate;
import com.mhd.alert.notice.AbstractAlertNoticeHandlerImpl;
import com.mhd.alert.notice.AlertNoticeException;
import com.mhd.alert.notice.AlertNoticeTypeEnum;
import com.mhd.boot.common.utils.StringUtils;
import com.mhd.boot.common.utils.json.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 飞书应用消息渠道通知处理器。
 *
 * <p>通过飞书开放平台应用消息接口发送告警卡片消息。流程：用 appId + appSecret 换取
 * tenant_access_token，再按接收类型（user / chat / department / all）调用消息发送接口。
 *
 * <p>支持四种接收模式：
 * <ul>
 *   <li>{@code larkReceiveType=0}：指定用户（userId，逗号分隔，单人单发、多人批量发）；</li>
 *   <li>{@code larkReceiveType=1}：指定群聊（chatId）；</li>
 *   <li>{@code larkReceiveType=2}：指定部门（partyId，逗号分隔，批量发）；</li>
 *   <li>{@code larkReceiveType=3}：全员（递归拉取员工列表后批量发）。</li>
 * </ul>
 *
 * @author zhao-hao-dong
 */
@Component
@Slf4j
public class FeiShuAppAlertNoticeHandlerImpl extends AbstractAlertNoticeHandlerImpl {

    /**
     * 获取 tenant_access_token 的接口 URL
     */
    private static final String TENANT_ACCESS_TOKEN_URL = "https://open.feishu.cn/open-apis/auth/v3/tenant_access_token/internal";

    /**
     * 拉取飞书应用员工列表的接口 URL
     */
    private static final String EMPLOYEE_URL = "https://open.feishu.cn/open-apis/ehr/v1/employees?status=2&status=4&user_id_type=user_id&page_size=100";

    /**
     * 发送飞书应用消息的接口 URL
     */
    private static final String APP_MESSAGE_URL = "https://open.feishu.cn/open-apis/im/v1/messages";

    /**
     * 接收类型：用户
     */
    private static final int USER_RECEIVE_TYPE = 0;

    /**
     * 接收类型：群聊
     */
    private static final int CHAT_RECEIVE_TYPE = 1;

    /**
     * 接收类型：部门
     */
    private static final int PART_RECEIVE_TYPE = 2;

    /**
     * 接收类型：全员
     */
    private static final int ALL_RECEIVE_TYPE = 3;

    /**
     * 发送飞书应用消息通知。
     *
     * <p>执行流程：
     * <ol>
     *   <li>用 appId + appSecret 换取 tenant_access_token；</li>
     *   <li>渲染模板为消息正文并构造卡片 JSON；</li>
     *   <li>按 larkReceiveType 分发到对应发送方法（单用户 / 批量用户 / 群聊 / 部门 / 全员）。</li>
     * </ol>
     *
     * @param receiver       通知接收人，含 appId/appSecret/larkReceiveType 及对应 id
     * @param noticeTemplate 通知模板，渲染为卡片 markdown 正文
     * @param alert          告警组，作为模板渲染数据来源
     * @throws AlertNoticeException 换 token 或发送失败时抛出
     */
    @Override
    public void send(NoticeReceiver receiver, NoticeTemplate noticeTemplate, AlertGroup alert) throws AlertNoticeException {
        String appId = receiver.getAppId();
        String appSecret = receiver.getAppSecret();
        Integer larkReceiveIdType = receiver.getLarkReceiveType();
        try {
            // 1. 换取 tenant_access_token
            String accessToken = getAccessToken(appId, appSecret);
            // 2. 渲染模板并构造卡片 JSON
            String notificationContent = JsonUtils.toJsonString(renderContent(noticeTemplate, alert));
            String messageContent = createLarkMessage(receiver.getUserId(), notificationContent);
            // 3. 按 larkReceiveType 分发到对应发送方法
            switch (larkReceiveIdType) {
                case USER_RECEIVE_TYPE -> {
                    String[] userIds = receiver.getUserId().split(",");
                    if (userIds.length == 1) {
                        sendLarkMessage(accessToken, "user_id", userIds[0], messageContent);
                    } else {
                        sendLarkUserBatchMessage(accessToken, userIds, messageContent);
                    }
                }
                case CHAT_RECEIVE_TYPE -> sendLarkMessage(accessToken, "chat_id", receiver.getChatId(), messageContent);
                case PART_RECEIVE_TYPE ->
                        sendLarkDepartmentBatchMessage(accessToken, receiver.getPartyId().split(","), messageContent);
                case ALL_RECEIVE_TYPE -> {
                    // 全员模式：递归拉取所有员工 ID 后批量发送
                    List<String> userIds = new ArrayList<>();
                    getLarkEmployeeUserIds(accessToken, null, userIds);
                    sendLarkUserBatchMessage(accessToken, userIds.toArray(new String[0]), messageContent);
                }
                default -> throw new AlertNoticeException("Invalid larkReceiveIdType: " + larkReceiveIdType);
            }
            log.debug("Send FeiShu App success, receiveType: {}", larkReceiveIdType);
        } catch (AlertNoticeException e) {
            throw e;
        } catch (Exception e) {
            throw new AlertNoticeException("[FeiShu App Notify Error] " + e.getMessage());
        }
    }

    /**
     * 向单个用户或群聊发送消息。
     *
     * @param accessToken     tenant_access_token
     * @param receiverIdType  接收 ID 类型：user_id 或 chat_id
     * @param receiverId      用户 ID 或群聊 ID
     * @param messageContent  卡片消息 JSON
     */
    private void sendLarkMessage(String accessToken, String receiverIdType, String receiverId, String messageContent) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        FeiShuAppMessageDto messageDto = FeiShuAppMessageDto.builder()
                .receiveId(receiverId)
                .content(messageContent)
                .build();
        HttpEntity<FeiShuAppMessageDto> request = new HttpEntity<>(messageDto, headers);
        call(APP_MESSAGE_URL + "?receive_id_type=" + receiverIdType, request, HttpMethod.POST, FeiShuAppResponse.class);
    }

    /**
     * 向多个用户批量发送消息。
     *
     * @param accessToken     tenant_access_token
     * @param userIds         用户 ID 数组
     * @param messageContent  卡片消息 JSON
     */
    private void sendLarkUserBatchMessage(String accessToken, String[] userIds, String messageContent) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        FeiShuAppBatchMessageDto batchMessageDto = FeiShuAppBatchMessageDto.builder()
                .userIds(userIds)
                .card(messageContent)
                .build();
        HttpEntity<FeiShuAppBatchMessageDto> request = new HttpEntity<>(batchMessageDto, headers);
        call(APP_MESSAGE_URL, request, HttpMethod.POST, FeiShuAppResponse.class);
    }

    /**
     * 向部门批量发送消息。
     *
     * @param accessToken     tenant_access_token
     * @param partyIds        部门 ID 数组
     * @param messageContent  卡片消息 JSON
     */
    private void sendLarkDepartmentBatchMessage(String accessToken, String[] partyIds, String messageContent) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        FeiShuAppBatchMessageDto batchMessageDto = FeiShuAppBatchMessageDto.builder()
                .departmentIds(partyIds)
                .card(messageContent)
                .build();
        HttpEntity<FeiShuAppBatchMessageDto> request = new HttpEntity<>(batchMessageDto, headers);
        call(APP_MESSAGE_URL, request, HttpMethod.POST, FeiShuAppResponse.class);
    }

    /**
     * 用 appId + appSecret 换取 tenant_access_token。
     *
     * @param appId     飞书应用 App ID
     * @param appSecret 飞书应用 App Secret
     * @return tenant_access_token
     */
    private String getAccessToken(String appId, String appSecret) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        FeiShuAppAccessTokenDto accessTokenRequest = FeiShuAppAccessTokenDto.builder()
                .appId(appId)
                .appSecret(appSecret)
                .build();
        HttpEntity<FeiShuAppAccessTokenDto> request = new HttpEntity<>(accessTokenRequest, headers);
        FeiShuAppAccessTokenResponse data = call(TENANT_ACCESS_TOKEN_URL, request, HttpMethod.POST, FeiShuAppAccessTokenResponse.class);
        return data.getTenantAccessToken();
    }

    /**
     * 递归拉取飞书应用的全部员工用户 ID（全员推送场景使用）。
     *
     * @param accessToken tenant_access_token
     * @param pageToken   分页标记，首页为 null
     * @param userIds     递归填充的用户 ID 集合
     */
    private void getLarkEmployeeUserIds(String accessToken, String pageToken, List<String> userIds) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        HttpEntity<String> request = new HttpEntity<>(headers);
        String url = StringUtils.isNotBlank(pageToken) ? EMPLOYEE_URL + "&page_token=" + pageToken : EMPLOYEE_URL;
        FeiShuAppEmployeeResponse employeeResponse = call(url, request, HttpMethod.GET, FeiShuAppEmployeeResponse.class);
        if (Objects.equals(employeeResponse.getCode(), 0)) {
            // 累加当前页员工 ID
            userIds.addAll(employeeResponse.getData().getItems().stream()
                    .map(FeiShuAppEmployeeResponse.Employee::getUserId).toList());
            // 有下一页时递归拉取
            if (Boolean.TRUE.equals(employeeResponse.getData().getHasMore())) {
                getLarkEmployeeUserIds(accessToken, employeeResponse.getData().getPageToken(), userIds);
            }
        }
    }

    /**
     * 统一的 HTTP 调用封装：发送请求并校验响应 code 是否为 0。
     *
     * @param url          请求 URL
     * @param request      请求实体
     * @param httpMethod   HTTP 方法
     * @param responseType 响应类型
     * @param <R>          响应类型泛型
     * @return 响应体
     * @throws AlertNoticeException HTTP 调用失败或响应 code 非 0 时抛出
     */
    private <R extends FeiShuAppResponse> R call(String url, HttpEntity<?> request, HttpMethod httpMethod, Class<R> responseType) {
        ResponseEntity<R> response = restTemplate.exchange(url, httpMethod, request, responseType);
        if (Objects.nonNull(response.getBody()) && !Objects.equals(response.getBody().getCode(), 0)) {
            log.warn("Send FeiShu App error: {}", response.getBody().getMsg());
            throw new AlertNoticeException("Http StatusCode " + response.getStatusCode()
                    + " Error: " + response.getBody().getMsg());
        }
        return response.getBody();
    }

    /**
     * 构造飞书应用消息卡片 JSON。
     *
     * <p>卡片含 markdown 正文 + 控制台跳转按钮。若配置了 userId，在正文末尾追加 @ 标签。
     *
     * @param userId              @ 的用户 ID（逗号分隔），为空则不 @
     * @param notificationContent JSON 字符串化后的正文
     * @return 完整的卡片消息 JSON 字符串
     */
    private String createLarkMessage(String userId, String notificationContent) {
        // 飞书应用卡片模板：与机器人卡片结构一致，简化为 markdown 正文 + 跳转按钮
        String larkCardMessage = """
                {
                "msg_type": "interactive",
                "card": {
                     "schema": "2.0",
                     "config": {"update_multi": true, "locales": ["en_us", "zh_cn"]},
                     "body": {
                         "direction": "vertical",
                         "padding": "12px 12px 12px 12px",
                         "elements": [
                             {"tag": "markdown", "content": "%s", "i18n_content": {"en_us": ""}, "text_align": "left", "text_size": "normal_v2", "margin": "0px 0px 0px 0px"},
                             {"tag": "hr", "margin": "0px 0px 0px 0px"},
                             {"tag": "column_set", "horizontal_align": "left", "columns": [
                                 {"tag": "column", "width": "weighted", "elements": [
                                     {"tag": "button", "text": {"tag": "plain_text", "content": "登入控制台"},
                                      "type": "default", "width": "default", "size": "medium",
                                      "behaviors": [{"type": "open_url", "default_url": "%s"}]}
                                 ], "weight": 1}
                             ]}
                         ]
                     },
                     "header": {"title": {"tag": "plain_text", "content": "告警通知"}, "template": "red"}
                 }
                }
                """;
        // 拼接 @ 用户标签
        String atUserElement = "";
        if (StringUtils.isNotBlank(userId)) {
            atUserElement = "\\n" + Arrays.stream(userId.split(","))
                    .map(id -> "<at user_id=\"" + id + "\"></at>")
                    .collect(Collectors.joining(" "));
        }
        // 去除 JSON 字符串化外层引号
        if (notificationContent.startsWith("\"") && notificationContent.endsWith("\"")) {
            notificationContent = StringUtils.removeStart(notificationContent, "\"");
            notificationContent = StringUtils.removeEnd(notificationContent, "\"");
        }
        return String.format(larkCardMessage,
                notificationContent.replace("\"", "\\\"") + atUserElement,
                alertProperties.getConsoleUrl());
    }

    /**
     * 返回飞书应用消息渠道类型枚举。
     *
     * @return {@link AlertNoticeTypeEnum#FEI_SHU_APP}
     */
    @Override
    public AlertNoticeTypeEnum type() {
        return AlertNoticeTypeEnum.FEI_SHU_APP;
    }

    /**
     * 飞书应用消息通用响应体。
     */
    @Data
    public static class FeiShuAppResponse {
        /**
         * 错误码，0 表示成功
         */
        private Integer code;

        /**
         * 错误信息
         */
        private String msg;
    }

    /**
     * tenant_access_token 响应体。
     */
    @Data
    public static class FeiShuAppAccessTokenResponse extends FeiShuAppResponse {
        /**
         * 租户 access token
         */
        @JsonProperty("tenant_access_token")
        private String tenantAccessToken;
    }

    /**
     * tenant_access_token 请求体。
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FeiShuAppAccessTokenDto {
        /**
         * 应用 App ID
         */
        @JsonProperty("app_id")
        private String appId;

        /**
         * 应用 App Secret
         */
        @JsonProperty("app_secret")
        private String appSecret;
    }

    /**
     * 单条消息请求体。
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FeiShuAppMessageDto {
        /**
         * 接收者 ID
         */
        @JsonProperty("receive_id")
        private String receiveId;

        /**
         * 消息内容 JSON
         */
        private String content;
    }

    /**
     * 批量消息请求体。
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FeiShuAppBatchMessageDto {
        /**
         * 用户 ID 列表（按用户批量发）
         */
        @JsonProperty("user_ids")
        private String[] userIds;

        /**
         * 部门 ID 列表（按部门批量发）
         */
        @JsonProperty("department_ids")
        private String[] departmentIds;

        /**
         * 卡片消息内容
         */
        private String card;
    }

    /**
     * 员工列表响应体。
     */
    @Data
    public static class FeiShuAppEmployeeResponse extends FeiShuAppResponse {
        /**
         * 响应数据
         */
        private EmployeeData data;

        /**
         * 员工列表数据。
         */
        @Data
        public static class EmployeeData {
            /**
             * 是否有下一页
             */
            private Boolean hasMore;

            /**
             * 下一页标记
             */
            @JsonProperty("page_token")
            private String pageToken;

            /**
             * 当前页员工列表
             */
            private List<Employee> items;
        }

        /**
         * 员工信息。
         */
        @Data
        public static class Employee {
            /**
             * 员工用户 ID
             */
            @JsonProperty("user_id")
            private String userId;
        }
    }
}
