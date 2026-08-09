package com.mhd.alert.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.concurrent.TimeUnit;

/**
 * Backoff utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BackoffUtils {
    public static boolean shouldContinueAfterBackoff(ExponentialBackoff backoff) {
        if (Thread.currentThread().isInterrupted()) {
            return false;
        }
        try {
            TimeUnit.MILLISECONDS.sleep(backoff.nextDelay());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
        return true;
    }
}