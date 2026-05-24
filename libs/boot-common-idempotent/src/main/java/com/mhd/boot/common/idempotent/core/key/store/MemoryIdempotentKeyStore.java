package com.mhd.boot.common.idempotent.core.key.store;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * @author zhao-hao-dong

 */
public class MemoryIdempotentKeyStore implements IdempotentKeyStore{
    /**
     * 初始化一个超大过期时间的本地定时缓存（默认不过期，靠手动清理）
     */
    private final TimedCache<String, Long> cache = CacheUtil.newTimedCache(Integer.MAX_VALUE);

    public MemoryIdempotentKeyStore() {
        // 启动缓存的定时清理任务，每1秒检查并移除过期的Key
        this.cache.schedulePrune(1L);
    }

    @Override
    public boolean saveIfAbsent(String key, long duration, TimeUnit timeUnit) {
        if(!StringUtils.hasText(key)) {
            return false;
        }
        // 锁定Key对应的字符串常量池对象，保证同一Key只有一个线程进入
        synchronized(key.intern()) {
            // 从缓存中获取Key对应的旧值（false表示不更新缓存过期时间）
            Long value = this.cache.get(key, false);
            if (value == null) {
                long timeOut = TimeUnit.MILLISECONDS.convert(duration, timeUnit);
                this.cache.put(key, System.currentTimeMillis(), timeOut);
                return true;
            }
        }
        return false;
    }

    @Override
    public void remove(String key) {
        this.cache.remove(key);
    }
}
