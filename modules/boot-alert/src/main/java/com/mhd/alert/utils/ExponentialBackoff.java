package com.mhd.alert.utils;

/**
 * Exponential backoff utility class.
 *
 * <p>Provides exponentially increasing delays starting from an initial value,
 * doubling with each call to {@link #nextDelay()} until reaching the maximum value.
 * Call {@link #reset()} to restart from the initial delay.</p>
 *
 * <p>Note: This class is <b>not</b> thread-safe. Each thread should use its own instance.</p>
 */
public final class ExponentialBackoff {
    private final long initial;
    private final long max;
    private long current;

    public ExponentialBackoff(long initial, long max) {
        if (initial <= 0 || max < initial) {
            throw new IllegalArgumentException("Invalid exponential backoff params");
        }
        this.initial = initial;
        this.max = max;
        this.current = initial;
    }

    public long nextDelay() {
        long delay = this.current;
        if (this.current <= this.max / 2) {
            this.current = this.current * 2;
        } else {
            this.current = this.max;
        }
        return delay;
    }

    public void reset() {
        this.current = this.initial;
    }
}
