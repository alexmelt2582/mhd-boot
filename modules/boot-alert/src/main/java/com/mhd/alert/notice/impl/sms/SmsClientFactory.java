package com.mhd.alert.notice.impl.sms;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 短信客户端工厂。
 *
 * <p>根据 {@link SmsProperties#getType()} 实例化对应的 {@link SmsClient} 实现，
 * 并缓存当前客户端实例。配置变更后需重启应用或调用 {@link #refresh()} 重新加载。
 *
 * <p>当前支持的服务商类型：
 * <ul>
 *   <li>{@code alibaba}：阿里云短信（{@link AliyunSmsClient}）</li>
 *   <li>{@code http}：通用 HTTP 短信网关（{@link HttpSmsClient}）</li>
 * </ul>
 *
 * <p>线程安全说明：{@link #currentSmsClient} 使用 {@code volatile} 保证可见性，
 * {@link #getSmsClient()} 使用 double-checked locking 避免重复加载。
 *
 * @author zhao-hao-dong
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SmsClientFactory {

    /**
     * 阿里云短信服务商标识
     */
    public static final String TYPE_ALIBABA = "alibaba";

    /**
     * 通用 HTTP 短信网关标识
     */
    public static final String TYPE_HTTP = "http";

    private final SmsProperties smsProperties;

    /**
     * Spring 注入的 HTTP 客户端，用于 {@link HttpSmsClient} 发送 HTTP 短信网关请求
     */
    private final RestTemplate restTemplate;

    /**
     * 当前生效的短信客户端，volatile 保证多线程可见性
     */
    private volatile SmsClient currentSmsClient;

    /**
     * 获取当前生效的短信客户端。
     *
     * <p>执行流程：
     * <ol>
     *   <li>缓存命中直接返回，避免重复加载；</li>
     *   <li>双重检查锁定下委托 {@link #loadClient()} 加载并缓存；</li>
     *   <li>加载失败或配置无效时返回 null，由调用方决定如何处理。</li>
     * </ol>
     *
     * @return 当前生效的短信客户端；未配置或禁用时返回 null
     */
    public SmsClient getSmsClient() {
        // 1. 缓存命中直接返回
        if (currentSmsClient != null) {
            return currentSmsClient;
        }
        // 2. 双重检查锁定：避免并发场景下重复加载
        synchronized (this) {
            if (currentSmsClient != null) {
                return currentSmsClient;
            }
            // 3. 加载并缓存客户端实例
            loadClient();
            return currentSmsClient;
        }
    }

    /**
     * 刷新短信客户端缓存。
     *
     * <p>配置变更后调用此方法强制重新加载，下次 {@link #getSmsClient()} 会按新配置实例化。
     */
    public void refresh() {
        synchronized (this) {
            currentSmsClient = null;
        }
        log.info("[SmsClientFactory] SMS client cache cleared, will reload on next access");
    }

    /**
     * 按 {@link SmsProperties#getType()} 实例化对应的短信客户端并缓存。
     *
     * <p>执行流程：
     * <ol>
     *   <li>校验是否启用与类型配置，缺失时记录警告并返回；</li>
     *   <li>按 type 分发到对应实现类构造器；</li>
     *   <li>未知类型记录警告，保留 null 客户端。</li>
     * </ol>
     */
    private void loadClient() {
        // 1. 前置校验：未启用或类型为空时直接返回
        if (!smsProperties.isEnable() || smsProperties.getType() == null || smsProperties.getType().isBlank()) {
            log.warn("[SmsClientFactory] SMS service disabled or type not configured");
            return;
        }
        // 2. 按 type 分发实例化
        String type = smsProperties.getType();
        switch (type) {
            case TYPE_ALIBABA -> currentSmsClient = new AliyunSmsClient(smsProperties.getAlibaba());
            case TYPE_HTTP -> currentSmsClient = new HttpSmsClient(smsProperties.getHttp(), restTemplate);
            default -> log.warn("[SmsClientFactory] Unsupported SMS provider type: {}", type);
        }
        if (currentSmsClient != null) {
            log.info("[SmsClientFactory] SMS client loaded, provider: {}", type);
        }
    }
}
