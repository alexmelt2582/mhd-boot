/*
 Navicat Premium Dump SQL

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80018 (8.0.18)
 Source Host           : localhost:3306
 Source Schema         : mhd-boot

 Target Server Type    : MySQL
 Target Server Version : 80018 (8.0.18)
 File Encoding         : 65001

 Date: 20/08/2026 07:50:51
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for hzb_alert_define_monitor_bind
-- ----------------------------
DROP TABLE IF EXISTS `hzb_alert_define_monitor_bind`;
CREATE TABLE `hzb_alert_define_monitor_bind`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `alert_define_id` bigint(20) NOT NULL COMMENT '告警规则ID',
  `monitor_id` bigint(20) NOT NULL COMMENT '监控项ID',
  `create_by` bigint(20) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_alert_define_id`(`alert_define_id` ASC) USING BTREE,
  INDEX `idx_monitor_id`(`monitor_id` ASC) USING BTREE,
  INDEX `idx_define_monitor`(`alert_define_id` ASC, `monitor_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '告警规则与监控项绑定关系表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for hzb_alert_event
-- ----------------------------
DROP TABLE IF EXISTS `hzb_alert_event`;
CREATE TABLE `hzb_alert_event`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `fingerprint` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '告警指纹(唯一标识)',
  `labels` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '标签(JSON), 如 {\"alertname\":\"HighCPUUsage\",\"priority\":\"critical\"}',
  `annotations` varchar(4096) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '注解(JSON), 如 {\"summary\":\"High CPU usage detected\"}',
  `content` varchar(4096) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '告警内容描述',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '状态: firing-告警中 / resolved-已恢复',
  `trigger_times` int(11) NULL DEFAULT NULL COMMENT '触发次数',
  `start_at` bigint(20) NULL DEFAULT NULL COMMENT '告警开始时间(毫秒时间戳)',
  `active_at` bigint(20) NULL DEFAULT NULL COMMENT '告警活跃时间(毫秒时间戳)',
  `end_at` bigint(20) NULL DEFAULT NULL COMMENT '告警结束时间(毫秒时间戳), resolved时才有值',
  `create_by` bigint(20) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `unique_fingerprint`(`fingerprint`(255) ASC) USING BTREE,
  INDEX `idx_alert_event_status`(`status` ASC) USING BTREE,
  INDEX `idx_alert_event_start_at`(`start_at` ASC) USING BTREE,
  INDEX `idx_alert_event_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '单条告警记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for hzb_alert_group
-- ----------------------------
DROP TABLE IF EXISTS `hzb_alert_group`;
CREATE TABLE `hzb_alert_group`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `group_key` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '分组键(唯一)',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '状态: firing / resolved',
  `group_labels` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '分组标签(JSON)',
  `common_labels` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '公共标签(JSON)',
  `common_annotations` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '公共注解(JSON)',
  `alert_fingerprints` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '关联告警指纹列表(JSON)',
  `create_by` bigint(20) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `unique_group_key`(`group_key`(255) ASC) USING BTREE,
  INDEX `idx_alert_group_status`(`status` ASC) USING BTREE,
  INDEX `idx_alert_group_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '分组告警记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for hzb_alert_group_converge
-- ----------------------------
DROP TABLE IF EXISTS `hzb_alert_group_converge`;
CREATE TABLE `hzb_alert_group_converge`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '策略名称',
  `group_labels` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '分组标签列表(JSON), 如 [\"instance\"]',
  `group_wait` bigint(20) NULL DEFAULT NULL COMMENT '首次发送分组告警前的等待时间(秒)',
  `group_interval` bigint(20) NULL DEFAULT NULL COMMENT '分组告警发送间隔(秒)',
  `repeat_interval` bigint(20) NULL DEFAULT NULL COMMENT '重复告警间隔(秒), 设为0则不重复',
  `enable` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用: 1-启用 0-禁用',
  `create_by` bigint(20) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_group_converge_name`(`name` ASC) USING BTREE,
  INDEX `idx_group_converge_enable`(`enable` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '告警分组收敛策略表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for hzb_alert_inhibit
-- ----------------------------
DROP TABLE IF EXISTS `hzb_alert_inhibit`;
CREATE TABLE `hzb_alert_inhibit`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '抑制规则名称',
  `source_labels` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '源告警匹配标签(JSON), 如 {\"severity\":\"critical\",\"instance\":\"web-01\"}',
  `target_labels` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '目标告警匹配标签(JSON), 如 {\"severity\":\"warning\",\"instance\":\"web-01\"}',
  `equal_labels` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '等同标签列表(JSON), 如 [\"instance\",\"job\"]',
  `enable` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用: 1-启用 0-禁用',
  `create_by` bigint(20) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_alert_inhibit_enable`(`enable` ASC) USING BTREE,
  INDEX `idx_alert_inhibit_name`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '告警抑制规则表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for hzb_alert_rule
-- ----------------------------
DROP TABLE IF EXISTS `hzb_alert_rule`;
CREATE TABLE `hzb_alert_rule`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '告警规则名称',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '规则类型: realtime_metric / periodic_metric / realtime_log / periodic_log',
  `expr` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '告警阈值表达式, 如 usage>90',
  `period` int(11) NULL DEFAULT NULL COMMENT '执行周期/窗口大小(秒), 用于周期规则或日志实时规则',
  `times` int(11) NULL DEFAULT NULL COMMENT '触发次数阈值, 达到后才真正触发告警',
  `labels` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '标签(JSON), 如 {\"status\":\"success\",\"env\":\"prod\",\"priority\":\"critical\"}',
  `annotations` varchar(4096) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '注解(JSON), 如 {\"summary\":\"High CPU usage\"}',
  `template` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '告警内容模板, 如 Instance {{ $labels.instance }} CPU usage is {{ $value }}%',
  `datasource` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '数据源类型, 如 PROMETHEUS',
  `enable` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用: 1-启用 0-禁用',
  `create_by` bigint(20) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_alert_rule_type`(`type` ASC) USING BTREE,
  INDEX `idx_alert_rule_enable`(`enable` ASC) USING BTREE,
  INDEX `idx_alert_rule_name`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '告警规则定义表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for hzb_alert_silence
-- ----------------------------
DROP TABLE IF EXISTS `hzb_alert_silence`;
CREATE TABLE `hzb_alert_silence`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '策略名称',
  `enable` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用: 1-启用 0-禁用',
  `match_all` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否匹配所有告警: 1-是 0-否',
  `type` tinyint(4) NOT NULL COMMENT '静默类型: 0-一次性 1-周期性',
  `times` int(11) NULL DEFAULT NULL COMMENT '已静默告警次数',
  `labels` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '匹配标签(JSON), 如 {\"key1\":\"value1\"}',
  `days` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '周期静默有效星期(JSON), 如 [0,1]; 7=周日 1=周一 ... 6=周六',
  `period_start` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '限制时段开始, 如 00:00:00',
  `period_end` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '限制时段结束, 如 23:59:59',
  `create_by` bigint(20) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_alert_silence_enable`(`enable` ASC) USING BTREE,
  INDEX `idx_alert_silence_type`(`type` ASC) USING BTREE,
  INDEX `idx_alert_silence_name`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '告警静默策略表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for hzb_notice_receiver
-- ----------------------------
DROP TABLE IF EXISTS `hzb_notice_receiver`;
CREATE TABLE `hzb_notice_receiver`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '接收人名称',
  `type` tinyint(4) NOT NULL COMMENT '通知方式: 0-SMS 1-Email 2-Webhook 3-微信公众号 4-企微机器人 5-钉钉机器人 6-飞书机器人 7-Telegram 8-Slack 9-Discord 10-企微应用消息 11-华为SMN 12-Server酱 13-Gotify 14-飞书应用消息 15-Ntfy',
  `phone` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '手机号(SMS时有效)',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '邮箱(Email时有效)',
  `hook_url` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Webhook URL',
  `hook_auth_type` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Webhook认证类型: None / Basic / Bearer',
  `hook_auth_token` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Webhook认证Token',
  `wechat_id` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'OpenID(微信公众号/企微/飞书机器人)',
  `app_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '飞书应用App ID',
  `access_token` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Access Token(钉钉机器人)',
  `tg_bot_token` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Telegram Bot Token',
  `tg_user_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Telegram User ID',
  `tg_message_thread_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Telegram Message Thread ID',
  `lark_receive_type` tinyint(4) NULL DEFAULT NULL COMMENT '飞书应用消息接收类型: 0-user 1-chat 2-party 3-all',
  `user_id` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '钉钉/飞书/企微用户ID',
  `chat_id` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '飞书应用消息chatId',
  `slack_web_hook_url` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Slack Webhook URL',
  `corp_id` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '企微企业ID',
  `agent_id` int(11) NULL DEFAULT NULL COMMENT '企微应用AgentId',
  `app_secret` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '企微应用Secret',
  `party_id` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '企微部门ID',
  `tag_id` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '企微标签ID',
  `discord_channel_id` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Discord频道ID',
  `discord_bot_token` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Discord Bot Token',
  `smn_ak` varchar(22) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '华为云SMN AccessKey',
  `smn_sk` varchar(42) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '华为云SMN SecretKey',
  `smn_project_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '华为云SMN项目ID',
  `smn_region` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '华为云SMN区域',
  `smn_topic_urn` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '华为云SMN TopicUrn',
  `server_chan_token` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Server酱Token',
  `gotify_token` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Gotify Token',
  `ntfy_server_url` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Ntfy服务器URL',
  `ntfy_topic` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Ntfy主题',
  `ntfy_token` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Ntfy访问Token',
  `create_by` bigint(20) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_notice_receiver_type`(`type` ASC) USING BTREE,
  INDEX `idx_notice_receiver_name`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '通知接收人表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for hzb_notice_rule
-- ----------------------------
DROP TABLE IF EXISTS `hzb_notice_rule`;
CREATE TABLE `hzb_notice_rule`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '策略名称',
  `receiver_id` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '接收人ID列表(JSON), 如 [4324324, 4324325]',
  `receiver_name` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '接收人名称列表(JSON), 如 [\"tom\",\"jerry\"]',
  `template_id` bigint(20) NULL DEFAULT NULL COMMENT '通知模板ID',
  `template_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '通知模板名称',
  `enable` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用: 1-启用 0-禁用',
  `match_specific_label` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否匹配指定的标签: 1-是 0-否(跳过标签匹配)\r\n1-匹配指定的标签\r\n0-跳过标签匹配',
  `labels` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '匹配标签(JSON), match_specific_label=1时有效',
  `days` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '有效星期(JSON), 如 [0,1]',
  `period_start` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '限制时段开始',
  `period_end` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '限制时段结束',
  `create_by` bigint(20) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_notice_rule_enable`(`enable` ASC) USING BTREE,
  INDEX `idx_notice_rule_name`(`name` ASC) USING BTREE,
  INDEX `idx_notice_rule_template_id`(`template_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '通知策略表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for hzb_notice_template
-- ----------------------------
DROP TABLE IF EXISTS `hzb_notice_template`;
CREATE TABLE `hzb_notice_template`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '模板名称',
  `type` tinyint(4) NOT NULL COMMENT '通知方式: 0-SMS 1-Email 2-Webhook 3-微信公众号 4-企微机器人 5-钉钉机器人 6-飞书机器人 7-Telegram 8-Slack 9-Discord 10-企微应用消息',
  `preset` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否预置模板: 1-预置 0-自定义',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '模板内容, 支持FreeMarker语法',
  `create_by` bigint(20) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_notice_template_type`(`type` ASC) USING BTREE,
  INDEX `idx_notice_template_preset`(`preset` ASC) USING BTREE,
  INDEX `idx_notice_template_name`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '通知模板表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_dict_item
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_item`;
CREATE TABLE `sys_dict_item`  (
  `dict_item_id` bigint(20) NOT NULL COMMENT '主键ID',
  `dict_sort` int(11) NULL DEFAULT 0 COMMENT '字典排序（数值越小越靠前）',
  `dict_label` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '字典标签（前端展示的名称）',
  `dict_value` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '字典键值（后端存储的实际值）',
  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '字典类型（关联 sys_dict_type 表的 dict_type）',
  `css_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '样式属性（其他CSS样式扩展）',
  `list_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '表格回显样式（如 primary, success, warning 等）',
  `is_default` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'N' COMMENT '是否默认（Y是 N否，默认N）',
  `create_by` bigint(20) NULL DEFAULT NULL COMMENT '创建者ID',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新者ID',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注信息',
  PRIMARY KEY (`dict_item_id`) USING BTREE,
  INDEX `idx_type_sort`(`dict_type` ASC, `dict_sort` ASC) USING BTREE,
  INDEX `idx_dict_value`(`dict_value` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统字典数据表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_dict_type
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type`  (
  `dict_id` bigint(20) NOT NULL COMMENT '主键ID',
  `dict_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '字典名称',
  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '字典类型（唯一标识，用于关联字典数据）',
  `create_by` bigint(20) NULL DEFAULT NULL COMMENT '创建者ID',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新者ID',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注信息',
  PRIMARY KEY (`dict_id`) USING BTREE,
  UNIQUE INDEX `uk_dict_type`(`dict_type` ASC) USING BTREE,
  INDEX `idx_dict_type`(`dict_type` ASC) USING BTREE,
  INDEX `idx_dict_name`(`dict_name` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统字典类型表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_notice
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice`  (
  `notice_id` bigint(20) NOT NULL COMMENT '公告ID',
  `notice_title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '公告标题',
  `notice_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '公告类型（如：1-通知, 2-公告）（字典获取）',
  `notice_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '公告内容',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '公告状态（如：0-正常, 1-关闭）',
  `create_by` bigint(20) NULL DEFAULT NULL COMMENT '创建者ID',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新者ID',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`notice_id`) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_create_by`(`create_by` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统公告表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_oper_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_oper_log`;
CREATE TABLE `sys_oper_log`  (
  `log_id` bigint(20) NOT NULL COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `operate_description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作描述',
  `operate_type` tinyint(4) NULL DEFAULT NULL COMMENT '操作类型：1-查询 2-新增 3-修改 4-删除 5-导出 6-导入 0-其他（字典获取）',
  `operate_module` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作模块',
  `operate_result` tinyint(4) NOT NULL COMMENT '操作结果：0-成功 1-失败',
  `operate_exception_detail` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '操作异常信息(堆栈)',
  `request_method` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求方法(如: GET, POST)',
  `request_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '请求参数(JSON格式)',
  `request_ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求IP',
  `request_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求地址(物理位置)',
  `request_browser` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求浏览器/客户端',
  `request_result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '请求结果(JSON格式)',
  `duration` bigint(20) NULL DEFAULT NULL COMMENT '请求耗时(单位:毫秒)',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间(精确到毫秒)',
  PRIMARY KEY (`log_id`) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_type_result`(`operate_type` ASC, `operate_result` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统-操作日志表' ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
