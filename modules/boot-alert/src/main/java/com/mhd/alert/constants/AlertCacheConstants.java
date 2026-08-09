package com.mhd.alert.constants;

/**
 * 告警模块缓存键常量定义。
 *
 * <p>集中管理 {@code AlertCacheFactory} 中各类缓存的 key，避免散落在各业务类中导致命名漂移。
 *
 * @author zhao-hao-dong
 */
public interface AlertCacheConstants {
    /**
     * 通知规则缓存键
     */
    String CACHE_NOTICE_RULE = "notice_rule";

    /**
     * 告警静默规则缓存键
     */
    String CACHE_ALERT_SILENCE = "alert_silence";
}
