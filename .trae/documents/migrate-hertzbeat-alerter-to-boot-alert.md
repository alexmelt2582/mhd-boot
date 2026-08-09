# 迁移 hertzbeat-alerter 至 boot-alert（包级分离 + 全量分阶段）

## Context（背景）

`mhd-boot/modules/boot-alert` 已从 `hertzbeat-alerter` 部分迁移，但告警收敛→分发→通知的**主链路是断裂的**：

- `AlarmCommonReduce` 调用 `alarmGroupReduce.processGroupAlert(...)`，但 `AlarmGroupReduce` 是**空类**
- `AlertNoticeDispatch` 被**整体注释掉**
- `AlarmInhibitReduce`、`AlarmSilenceReduce` **缺失**
- `AlertNoticeTypeEnum` 仅 SMS(0)/EMAIL(1)，需扩展到 16 种
- Email/SMS handler 是**空壳**（`send()` 为空），其余 14 个通知 handler **缺失**
- `NoticeReceiver` 实体**已完整迁移**（含全部 16 渠道字段），通知模板文件**已存在 13 个**（仅 WeChat、Ntfy 可能缺失）

目标：打通 `AlarmCommonReduce → AlarmGroupReduce → AlarmInhibitReduce → AlarmSilenceReduce → AlertNoticeDispatch → AlertNoticeHandler.send` 全链路，按 mhd-boot 代码风格实现，并对全模块补齐 JavaDoc。`SingleAlert→AlertEvent`、`AlertDefine→AlertRule`、`GroupAlert→AlertGroup` 已完成命名映射。

`java-doc-generator` 技能已修正（新增「多步骤方法编号流程注释模式」），所有新代码须遵循：JavaDoc 用 `<ol><li>` 列步骤，方法体用 `// 1. ... // 2. ...` 对应。

## 关键架构决策

1. **包级分离**（用户确认）：通知子系统全部置于 `com.mhd.alert.notice`（含 `notice/impl/<channel>/`），与 `reduce/`、`calculate/`、`service/` 分层。不新建 Maven 子模块。

2. **双轨模板渲染**：
   - `AlertTemplateUtils.render(template, Map)` —— 简单 `${prop.path}` 占位，用于**规则/告警内容**渲染（计算器已在用，保持不动）
   - `AbstractAlertNoticeHandlerImpl.renderContent(NoticeTemplate, AlertGroup)` —— **FreeMarker** 渲染，用于**通知模板**（模板文件已用 `<#list>`/`<#if>` 语法）。需在 `AbstractAlertNoticeHandlerImpl` 新增此方法，复用 Spring `FreeMarkerTemplateUtils`。需确认 boot-alert pom 含 `spring-boot-starter-freemarker`（或 `spring-context-support`），缺失则补充。

3. **实体类型适配**（不可照搬）：
   - `AlertInhibit.sourceLabels/targetLabels/equalLabels` 是 **JSON String**（非 Map/List）→ 需用 `JsonUtils.parseObject/parseArray` 解析
   - `AlertSilence.labels/days` 是 **JSON String**；`periodStart/periodEnd` 是 **String "HH:mm:ss"**（非 ZonedDateTime）→ 用 `LocalTime.parse`
   - `AlertEvent` 提供 `clone()`、`fingerprint`、`labels`、`annotations`、`content`、`status`、`triggerTimes`、`startAt`、`activeAt`、`endAt`
   - hertzbeat `CommonConstants.ALERT_STATUS_FIRING/RESOLVED` → mhd-boot `AlertStatusEnum.FIRING/RESOLVED.getCode()`

4. **依赖注入**：遵循 mhd-boot 风格——构造器注入 + `@RequiredArgsConstructor`（hertzbeat 用 `@Autowired` 字段注入，需转换）。

5. **线程池**：复用 `AlertThreadPoolConfig.executeNotify(Runnable)`（已存在）做异步通知派发。

## 迁移阶段

### 阶段 1：打通收敛分发链路（reduce + dispatch）

**修改/新建文件：**

- **`reduce/AlarmGroupReduce.java`**（修改，当前空类）—— 实现两个 `processGroupAlert` 重载：
  - `processGroupAlert(AlertEvent)`：单条告警按 groupLabels 聚合到 AlertGroup，转交 `AlarmInhibitReduce.inhibitAlarm`
  - `processGroupAlert(Map<String,String>, List<AlertEvent>)`：批量告警组装 AlertGroup，转交 inhibit
  - 依赖：`AlarmInhibitReduce`、`AlarmCacheManager`（已存在，用于分组累积）

- **`reduce/AlarmInhibitReduce.java`**（新建）—— 适配 hertzbeat 版，改用 mhd-boot 实体：
  - `inhibitAlarm(AlertGroup)`：源告警匹配缓存→过滤被抑制告警→转交 `AlarmSilenceReduce.silenceAlarm`
  - 用 `JsonUtils` 解析 `AlertInhibit.sourceLabels/targetLabels/equalLabels`
  - 源告警缓存 + 定时清理（ScheduledExecutorService，参照 hertzbeat 但简化）
  - 依赖：`AlertInhibitMapper`（或 `AlertInhibitService`）、`AlarmSilenceReduce`

- **`reduce/AlarmSilenceReduce.java`**（新建）—— 适配 hertzbeat 版：
  - `silenceAlarm(AlertGroup)`：匹配静默规则（matchAll/标签/时段）→ 未静默则转交 `AlertNoticeDispatch.dispatchAlarm`
  - 用 `JsonUtils` 解析 `AlertSilence.labels/days`；`LocalTime.parse` 处理 `periodStart/periodEnd`
  - 依赖：`AlertSilenceMapper`、`AlertNoticeDispatch`

- **`notice/AlertNoticeDispatch.java`**（修改，当前全注释）—— 重写实现：
  - `dispatchAlarm(AlertGroup)`：1.store 持久化 2.sendNotify 3.SSE 推送（`SseMessageUtils.publishAll`）
  - `sendNotify(AlertGroup)`：1.查通知规则 `NoticeRuleService.getReceiverFilterRule` 2.按规则裁剪 `scopeAlertToRule` 3.遍历接收人 `executeNotify` 异步发送
  - `sendNoticeMsg(NoticeReceiver, NoticeTemplate, AlertGroup)`：按 `receiver.getType()` 路由到 `AlertNoticeHandler`
  - `scopeAlertToRule(AlertGroup, NoticeRule)`：按规则标签过滤 alerts
  - 依赖：`AlertThreadPoolConfig`、`AlertStoreHandler`、`NoticeRuleService`、`NoticeTemplateService`、`NoticeReceiverService`、`Map<Integer,AlertNoticeHandler>`

- **`reduce/AlarmCommonReduce.java`**（微调）—— 确认调用 `alarmGroupReduce.processGroupAlert` 签名匹配；如需先经 inhibit/silence 链，调整委托目标。

### 阶段 2：通知基础设施

- **`notice/AlertNoticeTypeEnum.java`**（修改）—— 扩展到 16 值，对齐 `NoticeReceiver.type` 注释：
  `SMS(0), EMAIL(1), WEB_HOOK(2), WE_CHAT(3), WE_COM_ROBOT(4), DING_TALK_ROBOT(5), FLY_BOOK_ROBOT(6), TELEGRAM(7), SLACK(8), DISCORD(9), WE_COM_APP(10), HUAWEI_SMN(11), SERVER_CHAN(12), GOTIFY(13), FEI_SHU_APP(14), NTFY(15)`

- **`notice/AbstractAlertNoticeHandlerImpl.java`**（修改）—— 新增：
  - `renderContent(NoticeTemplate, AlertGroup)`：FreeMarker 渲染，构建 model（title/status/groupLabels/commonLabels/commonAnnotations/alerts/consoleUrl），复用 hertzbeat 思路但用 mhd-boot 实体
  - `escapeJsonStr(String)`：JSON 转义辅助
  - 保留现有 `restTemplate`、`alertProperties`、`log`

- **`notice/impl/CommonRobotNotifyResp.java`**（新建）—— 机器人通用响应 DTO（errcode/errmsg），用于解析 Slack/DingTalk/飞书等返回

- **pom.xml**（验证/补充）—— 确认 `spring-boot-starter-freemarker`；若无则添加。

### 阶段 3：16 个通知 Handler

按 `notice/impl/<channel>/` 子包组织（与现有 `email/`、`sms/` 一致）。每个 handler：`@Component`、构造器注入、`send()` 用 `renderContent` + `restTemplate`、`type()` 返回对应枚举、异常包 `AlertNoticeException`。

**修复现有空壳：**
- `notice/impl/email/EmailAlertNoticeHandlerImpl.java`（修改）—— 实现 `JavaMailSender` 发送
- `notice/impl/sms/SmsAlertNoticeHandlerImpl.java`（修改）—— 接入 SMS 客户端

**新建 14 个**（参照 hertzbeat 同名 handler，改 mhd-boot 实体/工具）：
- `notice/impl/webhook/WebHookAlertNoticeHandlerImpl.java`（type=2，最简，POST JSON + Basic/Bearer 认证）
- `notice/impl/wechat/WeChatAlertNoticeHandlerImpl.java`（type=3，公众号模板消息）
- `notice/impl/wecom/WeComRobotAlertNoticeHandlerImpl.java`（type=4，机器人 webhook）
- `notice/impl/dingtalk/DingTalkRobotAlertNoticeHandlerImpl.java`（type=5，签名+markdown）
- `notice/impl/flybook/FlyBookAlertNoticeHandlerImpl.java`（type=6，飞书机器人）
- `notice/impl/telegram/TelegramBotAlertNoticeHandlerImpl.java`（type=7，Bot API）
- `notice/impl/slack/SlackAlertNoticeHandlerImpl.java`（type=8，incoming webhook）
- `notice/impl/discord/DiscordBotAlertNoticeHandlerImpl.java`（type=9，webhook）
- `notice/impl/wecom/WeComAppAlertNoticeHandlerImpl.java`（type=10，应用消息+access_token）
- `notice/impl/huawei/HuaweiCloudSmnAlertNoticeHandlerImpl.java`（type=11，SMN SDK/签名）
- `notice/impl/serverchan/ServerChanAlertNoticeHandlerImpl.java`（type=12，Server酱）
- `notice/impl/gotify/GotifyAlertNoticeHandlerImpl.java`（type=13，Gotify REST）
- `notice/impl/feishu/FeiShuAppAlertNoticeHandlerImpl.java`（type=14，飞书应用消息）
- `notice/impl/ntfy/NtfyAlertNoticeHandlerImpl.java`（type=15，ntfy POST）

### 阶段 4：通知模板

- 现有 13 个模板（1-Email、2-Webhook、4-WeWorkRobot、5-DingTalk、6-FlyBook、7-Telegram、8-Slack、9-Discord、10-WeWorkApp、11-HuaweiSmn、12-ServerChan、13-Gotify、14-FeiShuApp）已用 FreeMarker 语法，**保留**。
- **新建缺失**：`3-WeChatTemplate.txt`（公众号）、`15-NtfyTemplate.txt`（ntfy），参照 hertzbeat 同名模板改用 mhd-boot 实体字段（`${alert.content}`、`${alert.triggerTimes}`、`${commonLabels.alertname}` 等）。

### 阶段 5：全模块 JavaDoc 补充

按 `java-doc-generator` 技能（含新增编号流程模式），对 boot-alert 下**所有类**补/修 JavaDoc：
- 新建类：编写时同步附规范 JavaDoc + 方法体内 `// 1. ... // 2. ...` 流程注释
- 已有类（entity/enums/mapper/service/controller/config/cache/utils/extern 等）：审查已有注释，补充缺失 JavaDoc 与方法内行内注释
- 优先级：公共 API > Entity 字段 > Service/Controller > 私有辅助方法
- 参照样板：`LogPeriodicAlertCalculator.java`

## 关键复用点（已存在，勿重复造）

| 已有资源 | 路径 | 用途 |
|---|---|---|
| `AlertTemplateUtils.render` | `utils/AlertTemplateUtils.java` | 规则/告警内容 `${}` 渲染 |
| `AlertThreadPoolConfig.executeNotify` | `config/AlertThreadPoolConfig.java` | 异步通知派发 |
| `AlertStoreHandler`/`DbAlertStoreHandlerImpl` | `store/` | 告警持久化 |
| `NoticeRuleService.getReceiverFilterRule` | `service/NoticeRuleService.java` | 查通知规则 |
| `NoticeTemplateService.getDefaultNoticeTemplateByType` | `service/NoticeTemplateService.java` | 默认模板 |
| `NoticeReceiverService.selectById` | `service/NoticeReceiverService.java` | 查接收人 |
| `AlertInhibitMapper`/`AlertSilenceMapper` | `mapper/` | 抑制/静默规则查询 |
| `AlarmCacheManager` | `cache/AlarmCacheManager.java` | 告警分组累积缓存 |
| `AlertStatusEnum` | `enums/AlertStatusEnum.java` | FIRING/RESOLVED 状态 |
| `JsonUtils` | boot-common | JSON String→Map/List 解析 |
| `SseMessageUtils.publishAll` | boot-common-sse | SSE 推送 |

## 验证

1. **编译**：`mvn -pl modules/boot-alert -am clean compile -DskipTests`（确认无编译错误、FreeMarker 依赖到位）
2. **链路完整性**：检查 `AlarmCommonReduce → AlarmGroupReduce → AlarmInhibitReduce → AlarmSilenceReduce → AlertNoticeDispatch → AlertNoticeHandler` 调用链无断点
3. **Handler 注册**：启动时 `AlertNoticeDispatch` 构造器注入 `List<AlertNoticeHandler>` 应收到 16 个 bean（看日志/断点）
4. **单测**：参照已有 `LogPeriodicAlertCalculatorTest`，为 `AlarmInhibitReduce`、`AlarmSilenceReduce`、`AlertNoticeDispatch`、`WebHookAlertNoticeHandlerImpl` 写最小单测（mock RestTemplate/Service）
5. **模板渲染**：用一条 AlertGroup 样本验证 FreeMarker `renderContent` 输出非空、`${alert.content}` 正确替换
6. **JavaDoc 自检**：`@param` 名称对齐签名、void 方法无 `@return`、多步骤方法有编号注释

## 执行顺序建议

阶段1 → 阶段2 → 阶段3（webhook/dingtalk/slack/telegram 优先，再补其余）→ 阶段4 → 阶段5（与各阶段同步进行，新建类即时补注释）。每阶段完成后可单独编译验证。
