package com.mhd.alert.notice;

import com.mhd.alert.config.AlertProperties;
import com.mhd.alert.entity.AlertGroup;
import com.mhd.alert.entity.NoticeTemplate;
import freemarker.cache.StringTemplateLoader;
import freemarker.core.TemplateClassResolver;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 通知处理器抽象基类。
 *
 * <p>为所有 {@link AlertNoticeHandler} 实现提供两类共享能力：
 * <ul>
 *   <li>{@link #renderContent(NoticeTemplate, AlertGroup)}：基于 FreeMarker 渲染通知模板，
 *       将告警组字段注入模型，输出各渠道所需的消息体（JSON / HTML / Markdown 等）；</li>
 *   <li>{@link #escapeJsonStr(String)}：JSON 字符串转义辅助，用于在拼接 JSON 时
 *       对告警内容做安全转义，避免换行 / 引号破坏 JSON 结构。</li>
 * </ul>
 *
 * <p>子类通过 {@link #restTemplate} 发送 HTTP 请求，通过 {@link #alertProperties}
 * 读取控制台地址等配置。二者由 Spring 自动注入，子类无需重复声明。
 *
 * <p>注意：{@code restTemplate} 与 {@code alertProperties} 采用字段注入，是为了让 16 个
 * 渠道子类无需在构造器中透传这些共享依赖，降低子类构造器复杂度。
 *
 * @author zhao-hao-dong
 */
public abstract class AbstractAlertNoticeHandlerImpl implements AlertNoticeHandler {

    /**
     * 通知标题，注入 FreeMarker 模型供模板引用（如邮件主题）
     */
    protected static final String NOTIFY_TITLE = "告警通知";

    /**
     * FreeMarker 数字格式：避免逗号分隔千分位（如 1,000）破坏 JSON 数字字段
     */
    private static final String NUMBER_FORMAT = "0";

    /**
     * FreeMarker 版本，使用 2.3.0 兼容模式
     */
    private static final Version FREEMARKER_VERSION = Configuration.VERSION_2_3_0;

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * HTTP 客户端，子类用于向各渠道 webhook / API 发送请求
     */
    @Autowired
    protected RestTemplate restTemplate;

    /**
     * 告警模块配置，提供控制台地址等模板渲染所需上下文
     */
    @Autowired
    protected AlertProperties alertProperties;

    /**
     * 渲染通知模板：将告警组字段注入 FreeMarker 模型并执行模板，返回渲染后的字符串。
     *
     * <p>执行流程：
     * <ol>
     *   <li>构建模型 Map：title / status / groupLabels / commonLabels /
     *       commonAnnotations / alerts / consoleUrl；</li>
     *   <li>用 {@link StringTemplateLoader} 加载模板内容，避免模板缓存串号；</li>
     *   <li>配置 FreeMarker：禁用类解析（安全）、数字格式 0、HTML 安全的异常处理；</li>
     *   <li>执行模板渲染并压缩多余空行，返回最终消息体。</li>
     * </ol>
     *
     * <p>每次调用新建 {@link Configuration} 与 {@link StringTemplateLoader}，保证线程安全，
     * 牺牲少量性能换取实现简单（参照 hertzbeat 实现，后续可优化为带版本号的缓存）。
     *
     * @param noticeTemplate 通知模板，其 {@code content} 为 FreeMarker 模板字符串
     * @param alert          告警组，作为模型数据来源
     * @return 渲染后的通知内容
     * @throws AlertNoticeException 模板渲染失败时抛出，由上层捕获并记录
     */
    protected String renderContent(NoticeTemplate noticeTemplate, AlertGroup alert) throws AlertNoticeException {
        // 1. 构建模型：告警组各字段 + 控制台地址 + 标题
        Map<String, Object> model = new HashMap<>(16);
        model.put("title", NOTIFY_TITLE);
        model.put("status", alert.getStatus());
        model.put("groupLabels", alert.getGroupLabels());
        model.put("commonLabels", alert.getCommonLabels());
        model.put("commonAnnotations", alert.getCommonAnnotations());
        model.put("alerts", alert.getAlerts());
        if (alertProperties != null) {
            model.put("consoleUrl", alertProperties.getConsoleUrl());
        }
        // 2. 用 StringTemplateLoader 加载模板内容，每次使用唯一名称避免缓存命中旧模板
        StringTemplateLoader stringLoader = new StringTemplateLoader();
        String templateName = "alertNoticeTemplate_" + System.nanoTime();
        stringLoader.putTemplate(templateName, noticeTemplate.getContent());
        // 3. 配置 FreeMarker：禁用类解析防止模板注入、数字格式 0 避免 JSON 数字被千分位破坏
        Configuration cfg = new Configuration(FREEMARKER_VERSION);
        cfg.setNumberFormat(NUMBER_FORMAT);
        cfg.setNewBuiltinClassResolver(TemplateClassResolver.SAFER_RESOLVER);
        cfg.setTemplateLoader(stringLoader);
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        try {
            freemarker.template.Template template = cfg.getTemplate(templateName, Locale.CHINESE);
            // 4. 执行渲染并压缩连续空行为单个换行，保持消息体紧凑
            String rendered = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
            return rendered.replaceAll("((\r\n)|\n)[\\s\t ]*(\\1)+", "$1");
        } catch (TemplateException | IOException e) {
            throw new AlertNoticeException("Render notice template failed: " + e.getMessage());
        }
    }

    /**
     * 对字符串进行 JSON 转义，使其可安全嵌入 JSON 字符串值。
     *
     * <p>转义 {@code "}、{@code \}、控制字符（\b \f \n \r \t），用于手工拼接 JSON
     * 场景（如部分机器人协议需自定义 JSON 结构时）。若使用模板渲染则无需手动调用。
     *
     * @param jsonStr 待转义的原始字符串
     * @return 转义后的字符串，入参为 null 时原样返回 null
     */
    protected String escapeJsonStr(String jsonStr) {
        if (jsonStr == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(jsonStr.length() + 16);
        for (char c : jsonStr.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }
}
