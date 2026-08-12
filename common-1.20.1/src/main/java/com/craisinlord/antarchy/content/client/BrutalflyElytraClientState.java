package com.craisinlord.antarchy.content.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.util.Mth;

public final class BrutalflyElytraClientState {
    private static final Map<Integer, AnimationState> ACTIVE_ANIMATIONS = new ConcurrentHashMap<>();

    private BrutalflyElytraClientState() {
    }

    public static void trigger(int entityId, int durationTicks, float strength) {
        if (durationTicks <= 0) {
            ACTIVE_ANIMATIONS.remove(entityId);
            return;
        }
        ACTIVE_ANIMATIONS.put(entityId, new AnimationState(durationTicks, durationTicks, strength));
    }

    public static AnimationState get(int entityId) {
        return ACTIVE_ANIMATIONS.get(entityId);
    }

    public static void tick() {
        ACTIVE_ANIMATIONS.entrySet().removeIf(entry -> entry.getValue().tick() <= 0);
    }

    public static final class AnimationState {
        private final int durationTicks;
        private int remainingTicks;
        private final float strength;

        private AnimationState(int durationTicks, int remainingTicks, float strength) {
            this.durationTicks = Math.max(1, durationTicks);
            this.remainingTicks = Math.max(0, remainingTicks);
            this.strength = strength;
        }

        public float progress(float partialTick) {
            float remaining = Mth.clamp(this.remainingTicks - partialTick, 0.0F, (float) this.durationTicks);
            return 1.0F - remaining / (float) this.durationTicks;
        }

        public float strength() {
            return this.strength;
        }

        private int tick() {
            this.remainingTicks--;
            return this.remainingTicks;
        }
    }
}
