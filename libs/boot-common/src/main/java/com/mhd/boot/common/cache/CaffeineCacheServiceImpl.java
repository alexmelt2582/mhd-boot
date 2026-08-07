package com.mhd.boot.common.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.time.Duration;

/**
 * @author zhao-hao-dong
 */
public class CaffeineCacheServiceImpl<K, V> implements CommonCacheService<K, V> {
    private final Cache<K, V> cache;

    public CaffeineCacheServiceImpl(final int initialCapacity,
                                    final long maximumSize,
                                    final Duration expireAfterWrite,
                                    final boolean useWeakKey) {
        if (useWeakKey) {
            this.cache = Caffeine.newBuilder()
                    .weakKeys()
                    .initialCapacity(initialCapacity)
                    .maximumSize(maximumSize)
                    .expireAfterWrite(expireAfterWrite)
                    .build();
        } else {
            this.cache = Caffeine.newBuilder()
                    .initialCapacity(initialCapacity)
                    .maximumSize(maximumSize)
                    .expireAfterWrite(expireAfterWrite)
                    .build();
        }
    }

    @Override
    public V get(K key) {
        return cache.getIfPresent(key);
    }

    @Override
    public void put(K key, V value) {
        cache.put(key, value);
    }

    @Override
    public V putAndGetOld(K key, V value) {
        V oldValue = cache.getIfPresent(key);
        cache.put(key, value);
        return oldValue;
    }

    @Override
    public boolean containsKey(K key) {
        return cache.asMap().containsKey(key);
    }

    @Override
    public V remove(K key) {
        V value = cache.getIfPresent(key);
        this.cache.invalidate(key);
        this.cache.cleanUp();
        return value;
    }

    @Override
    public boolean clear() {
        this.cache.invalidateAll();
        this.cache.cleanUp();
        return true;
    }
}
