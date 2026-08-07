package com.mhd.alert.cache;

import com.mhd.alert.constants.AlertCacheConstants;
import com.mhd.alert.entity.NoticeRule;
import com.mhd.boot.common.cache.CaffeineCacheServiceImpl;
import com.mhd.boot.common.cache.CommonCacheService;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.util.List;

/**
 * @author zhao-hao-dong
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AlertCacheFactory {
    private static final CommonCacheService<String, Object> COMMON_CACHE =
            new CaffeineCacheServiceImpl<>(1, 1000, Duration.ofDays(1), false);

    @SuppressWarnings("unchecked")
    public static List<NoticeRule> getNoticeCache() {
        return (List<NoticeRule>) COMMON_CACHE.get(AlertCacheConstants.CACHE_NOTICE_RULE);
    }

    public static void setNoticeCache(List<NoticeRule> noticeRules) {
        COMMON_CACHE.put(AlertCacheConstants.CACHE_NOTICE_RULE, noticeRules);
    }

    public static void clearNoticeCache() {
        COMMON_CACHE.remove(AlertCacheConstants.CACHE_NOTICE_RULE);
    }

}
