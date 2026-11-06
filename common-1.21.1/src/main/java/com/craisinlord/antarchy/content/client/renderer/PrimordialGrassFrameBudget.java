package com.craisinlord.antarchy.content.client.renderer;

final class PrimordialGrassFrameBudget {
    private static final long SHARE_DIVISOR = 10L;
    private static final long BACKGROUND_DIVISOR = 40L;
    private static final long MAX_NANOS = 3_000_000L;

    private long lastRefill = Long.MIN_VALUE;
    private long available;

    void refill(long now) {
        refill(now, true);
    }

    void refill(long now, boolean urgent) {
        if (lastRefill == Long.MIN_VALUE) {
            lastRefill = now;
            available = MAX_NANOS;
            return;
        }
        long elapsed = Math.max(0L, now - lastRefill);
        lastRefill = now;
        available = Math.min(MAX_NANOS, available + elapsed / (urgent ? SHARE_DIVISOR : BACKGROUND_DIVISOR));
    }

    long available() {
        return Math.max(0L, available);
    }

    void spend(long nanos) {
        available -= nanos;
    }
}
