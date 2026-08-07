package com.mhd.boot.common.cache;

/**
 * @author zhao-hao-dong
 */
public interface CommonCacheService<K, V> {
    V get(K key);

    void put(K key, V value);

    V putAndGetOld(K key, V value);

    boolean containsKey(K key);

    V remove(K key);

    boolean clear();
}
