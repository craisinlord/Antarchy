package com.craisinlord.antarchy.content.client;

public final class HerculesBeetleImpactShakeClientState {
    private static int shakeTicks;

    private HerculesBeetleImpactShakeClientState() {
    }

    public static void trigger(int ticks) {
        shakeTicks = Math.max(shakeTicks, Math.max(0, ticks));
    }

    public static void tick() {
        if (shakeTicks > 0) {
            shakeTicks--;
        }
    }

    public static int getTicks() {
        return shakeTicks;
    }

    public static void clear() {
        shakeTicks = 0;
    }
}
