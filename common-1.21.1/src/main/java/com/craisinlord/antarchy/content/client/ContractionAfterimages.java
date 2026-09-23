package com.craisinlord.antarchy.content.client;

import com.craisinlord.antarchy.content.effect.RoyalEffectHooks;
import com.craisinlord.antarchy.content.item.TemporalTunerItem;
import com.craisinlord.antarchy.content.time.TimeDilationApi;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.Holder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class ContractionAfterimages {
    public static final int MAX_SAMPLES = 10;
    public static final int SAMPLE_LIFETIME_TICKS = 13;

    private static final Map<LivingEntity, Deque<Sample>> HISTORY = new WeakHashMap<>();

    private ContractionAfterimages() {
    }

    public static void tick(ClientLevel level) {
        int now = (int) (level.getGameTime() & 0x7fffffffL);

        Iterator<Map.Entry<LivingEntity, Deque<Sample>>> iterator = HISTORY.entrySet().iterator();
        while (iterator.hasNext()) {
            LivingEntity tracked = iterator.next().getKey();
            if (tracked == null || tracked.isRemoved() || !tracked.isAlive() || !isActive(tracked)) {
                iterator.remove();
            }
        }
    }

    public static void observe(LivingEntity living) {
        if (!isActive(living)) {
            return;
        }
        int now = (int) (living.level().getGameTime() & 0x7fffffffL);
        Deque<Sample> samples = HISTORY.computeIfAbsent(living, ignored -> new ArrayDeque<>());
        samples.addFirst(new Sample(living, now));
        while (samples.size() > MAX_SAMPLES) {
            samples.removeLast();
        }
    }

    public static Deque<Sample> samples(LivingEntity entity) {
        return HISTORY.get(entity);
    }

    public static boolean isActive(LivingEntity living) {
        Holder<MobEffect> holder = RoyalEffectHooks.contractedHolder();
        boolean contracted = holder != null && living.hasEffect(holder);
        boolean tuned = living instanceof net.minecraft.world.entity.player.Player player
                && TemporalTunerItem.isAvailable(player)
                && TimeDilationApi.getRate(living) > 1.0D;
        return !living.isInvisible() && (contracted || tuned);
    }

    public static float fade(Sample sample, int now) {
        float elapsed = now - sample.recordedTick;
        return Math.max(0.0F, 1.0F - elapsed / SAMPLE_LIFETIME_TICKS);
    }

    public static void clear() {
        HISTORY.clear();
    }

    public static final class Sample {
        public final double x;
        public final double y;
        public final double z;
        public final float bodyYaw;
        public final float headYaw;
        public final float xRot;
        public final float limbPos;
        public final float limbSpeed;
        public final float ageInTicks;
        public final int recordedTick;

        Sample(LivingEntity entity, int recordedTick) {
            this.x = entity.getX();
            this.y = entity.getY();
            this.z = entity.getZ();
            this.bodyYaw = entity.yBodyRot;
            this.headYaw = entity.getYHeadRot();
            this.xRot = entity.getXRot();
            this.limbPos = entity.walkAnimation.position();
            this.limbSpeed = Math.min(entity.walkAnimation.speed(), 1.0F);
            this.ageInTicks = entity.tickCount;
            this.recordedTick = recordedTick;
        }
    }
}
