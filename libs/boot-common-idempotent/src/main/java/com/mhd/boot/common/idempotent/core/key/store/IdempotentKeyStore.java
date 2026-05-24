package com.mhd.boot.common.idempotent.core.key.store;

import java.util.concurrent.TimeUnit;

/**
 * @author zhao-hao-dong

 */
public interface IdempotentKeyStore {
    boolean saveIfAbsent(String key, long duration, TimeUnit timeUnit);

    void remove(String key);
}
