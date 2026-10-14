package com.craisinlord.antarchy.content.client;

public final class KingJudgmentFlashClientState {
    private static int flashTicks;
    private static int flashDuration;

    private KingJudgmentFlashClientState() {
    }

    public static void trigger(int ticks) {
        int duration = Math.max(0, ticks);
        if (duration >= flashTicks) {
            flashTicks = duration;
            flashDuration = duration;
        }
    }

    public static void tick() {
        if (flashTicks > 0) {
            flashTicks--;
        }
    }

    public static float alpha() {
        if (flashTicks <= 0 || flashDuration <= 0) {
            return 0.0F;
        }
        float remaining = flashTicks / (float) flashDuration;
        return Math.min(0.92F, 0.92F * remaining * remaining);
    }

    public static void clear() {
        flashTicks = 0;
        flashDuration = 0;
    }
}
