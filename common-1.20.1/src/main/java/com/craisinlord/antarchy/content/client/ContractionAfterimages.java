package com.craisinlord.antarchy.content.client;

import com.craisinlord.antarchy.content.effect.RoyalEffectHooks;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffect;

public final class ContractionAfterimages {
    public static final int MAX_SAMPLES = 10;
    public static final int SAMPLE_LIFETIME_TICKS = 13;
    private static final Map<LivingEntity, Deque<Sample>> HISTORY = new WeakHashMap<>();

    private ContractionAfterimages() {}

    public static void tick(ClientLevel level) {
        MobEffect contracted = RoyalEffectHooks.contractedHolder();
        int now = (int) (level.getGameTime() & 0x7fffffffL);
        Iterator<Map.Entry<LivingEntity, Deque<Sample>>> iterator = HISTORY.entrySet().iterator();
        while (iterator.hasNext()) {
            LivingEntity tracked = iterator.next().getKey();
            if (tracked == null || tracked.isRemoved() || !tracked.isAlive() || contracted == null
                    || !tracked.hasEffect(contracted)) {
                iterator.remove();
            }
        }
        if (contracted == null) return;
        for (Entity entity : level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living) || living.isInvisible() || !living.hasEffect(contracted)) continue;
            Deque<Sample> samples = HISTORY.computeIfAbsent(living, ignored -> new ArrayDeque<>());
            samples.addFirst(new Sample(living, now));
            while (samples.size() > MAX_SAMPLES) samples.removeLast();
        }
    }

    public static Deque<Sample> samples(LivingEntity entity) { return HISTORY.get(entity); }

    public static float fade(Sample sample, int now) {
        return Math.max(0.0F, 1.0F - (now - sample.recordedTick) / (float) SAMPLE_LIFETIME_TICKS);
    }

    public static void clear() { HISTORY.clear(); }

    public static final class Sample {
        public final double x, y, z;
        public final float bodyYaw, headYaw, xRot, limbPos, limbSpeed, ageInTicks;
        public final int recordedTick;

        Sample(LivingEntity entity, int recordedTick) {
            this.x = entity.getX(); this.y = entity.getY(); this.z = entity.getZ();
            this.bodyYaw = entity.yBodyRot; this.headYaw = entity.getYHeadRot(); this.xRot = entity.getXRot();
            this.limbPos = entity.walkAnimation.position();
            this.limbSpeed = Math.min(entity.walkAnimation.speed(), 1.0F);
            this.ageInTicks = entity.tickCount; this.recordedTick = recordedTick;
        }
    }
}
