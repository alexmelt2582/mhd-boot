package com.mhd.alert.cache;

import com.mhd.alert.constants.AlertCacheConstants;
import com.mhd.alert.entity.AlertSilence;
import com.mhd.alert.entity.NoticeRule;
import com.mhd.boot.common.cache.CaffeineCacheServiceImpl;
import com.mhd.boot.common.cache.CommonCacheService;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.util.List;

/**
 * 告警模块缓存工厂。
 *
 * <p>基于 {@link CaffeineCacheServiceImpl} 提供单进程内的本地缓存，集中托管
 * 通知规则、静默规则等变更频率较低的全量配置，避免每次告警收敛时重复查库。
 * 缓存容量 1000、TTL 1 天，由调用方在配置变更时主动失效。
 *
 * @author zhao-hao-dong
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AlertCacheFactory {
    private static final CommonCacheService<String, Object> COMMON_CACHE =
            new CaffeineCacheServiceImpl<>(1, 1000, Duration.ofDays(1), false);

    /**
     * 获取通知规则缓存。
     *
     * @return 缓存的通知规则列表，未命中返回 null
     */
    @SuppressWarnings("unchecked")
    public static List<NoticeRule> getNoticeCache() {
        return (List<NoticeRule>) COMMON_CACHE.get(AlertCacheConstants.CACHE_NOTICE_RULE);
    }

    /**
     * 写入通知规则缓存。
     *
     * @param noticeRules 通知规则列表
     */
    public static void setNoticeCache(List<NoticeRule> noticeRules) {
        COMMON_CACHE.put(AlertCacheConstants.CACHE_NOTICE_RULE, noticeRules);
    }

    /**
     * 失效通知规则缓存。
     */
    public static void clearNoticeCache() {
        COMMON_CACHE.remove(AlertCacheConstants.CACHE_NOTICE_RULE);
    }

    /**
     * 获取告警静默规则缓存。
     *
     * @return 缓存的静默规则列表，未命中返回 null
     */
    @SuppressWarnings("unchecked")
    public static List<AlertSilence> getAlertSilenceCache() {
        return (List<AlertSilence>) COMMON_CACHE.get(AlertCacheConstants.CACHE_ALERT_SILENCE);
    }

    /**
     * 写入告警静默规则缓存。
     *
     * @param alertSilenceList 静默规则列表
     */
    public static void setAlertSilenceCache(List<AlertSilence> alertSilenceList) {
        COMMON_CACHE.put(AlertCacheConstants.CACHE_ALERT_SILENCE, alertSilenceList);
    }

    /**
     * 失效告警静默规则缓存。
     */
    public static void clearAlertSilenceCache() {
        COMMON_CACHE.remove(AlertCacheConstants.CACHE_ALERT_SILENCE);
    }

}
