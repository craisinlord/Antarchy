package com.craisinlord.antarchy.content.client;

import com.craisinlord.antarchy.content.time.TimeDilationFieldSnapshot;
import com.craisinlord.antarchy.content.time.TimeDilationFieldSnapshotSampler;
import com.craisinlord.antarchy.content.time.TimeDilationMath;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.util.Mth;

public final class ClientTimeDilationTicker {
    private static volatile List<TimeDilationFieldSnapshot> activeFields = List.of();
    private static ClientLevel activeLevel;
    private static final Map<ItemEntity, ItemSpinState> ITEM_SPIN_STATES = new WeakHashMap<>();
    private static final Map<LivingEntity, AttackAnimationState> ATTACK_ANIMATION_STATES = new WeakHashMap<>();
    private static final Map<Entity, AnimationClockState> ANIMATION_CLOCKS = new WeakHashMap<>();

    private ClientTimeDilationTicker() {
    }

    public static void tick(ClientLevel level) {
        if (activeLevel != level) {
            activeLevel = level;
            activeFields = List.of();
            ITEM_SPIN_STATES.clear();
            ATTACK_ANIMATION_STATES.clear();
            ANIMATION_CLOCKS.clear();
            com.craisinlord.antarchy.content.time.TimeDilationApi.clearSyncedClientRates();
        }
        // Entity rates come from the server. Field snapshots are used only for local effects.
    }

    public static float dilateAnimationTime(Entity entity, float vanillaTime) {
        double rate = com.craisinlord.antarchy.content.time.TimeDilationApi.getRate(entity);
        if (rate >= TimeDilationMath.NORMAL_RATE) {
            ANIMATION_CLOCKS.remove(entity);
            return vanillaTime;
        }
        AnimationClockState state = ANIMATION_CLOCKS.computeIfAbsent(entity,
                ignored -> new AnimationClockState(vanillaTime));
        float delta = vanillaTime - state.lastVanillaTime;
        if (delta >= 0.0F) {
            state.temporalTime += delta * (float) rate;
        }
        state.lastVanillaTime = vanillaTime;
        return state.temporalTime;
    }

    public static float dilateAttackAnimation(LivingEntity entity, float partialTick, float vanillaAnimation) {
        double rate = com.craisinlord.antarchy.content.time.TimeDilationApi.getRate(entity);
        if (rate >= TimeDilationMath.NORMAL_RATE) {
            ATTACK_ANIMATION_STATES.remove(entity);
            return vanillaAnimation;
        }
        AttackAnimationState state = ATTACK_ANIMATION_STATES.computeIfAbsent(
                entity, ignored -> new AttackAnimationState(entity.tickCount, vanillaAnimation)
        );
        if (state.lastTick != entity.tickCount) {
            state.previous = state.current;
            state.current += (vanillaAnimation - state.current) * (float) rate;
            state.lastTick = entity.tickCount;
        }
        return state.previous + (state.current - state.previous) * partialTick;
    }

    public static float dilateRotation(LivingEntity entity, float previous, float current, float partialTick) {
        double rate = com.craisinlord.antarchy.content.time.TimeDilationApi.getRate(entity);
        if (rate >= TimeDilationMath.NORMAL_RATE) {
            return Mth.rotLerp(partialTick, previous, current);
        }
        return previous + Mth.wrapDegrees(current - previous) * partialTick * (float) rate;
    }

    public static float dilatePitch(LivingEntity entity, float previous, float current, float partialTick) {
        double rate = com.craisinlord.antarchy.content.time.TimeDilationApi.getRate(entity);
        if (rate >= TimeDilationMath.NORMAL_RATE) {
            return Mth.lerp(partialTick, previous, current);
        }
        return previous + (current - previous) * partialTick * (float) rate;
    }

    public static float dilateWalkAmount(LivingEntity entity, float amount) {
        double rate = com.craisinlord.antarchy.content.time.TimeDilationApi.getRate(entity);
        return rate >= TimeDilationMath.NORMAL_RATE ? amount : amount * (float) rate;
    }

    public static float dilateItemSpin(ItemEntity item, float vanillaSpin) {
        double rate = com.craisinlord.antarchy.content.time.TimeDilationApi.getRate(item);
        if (rate >= TimeDilationMath.NORMAL_RATE) {
            ITEM_SPIN_STATES.remove(item);
            return vanillaSpin;
        }
        ItemSpinState state = ITEM_SPIN_STATES.computeIfAbsent(item, ignored -> new ItemSpinState(vanillaSpin));
        float rawDelta = vanillaSpin - state.lastVanillaSpin;
        state.lastVanillaSpin = vanillaSpin;
        state.temporalSpin += rawDelta * (float) rate;
        return state.temporalSpin;
    }

    private static final class ItemSpinState {
        private float lastVanillaSpin;
        private float temporalSpin;

        private ItemSpinState(float initialSpin) {
            this.lastVanillaSpin = initialSpin;
            this.temporalSpin = initialSpin;
        }
    }

    private static final class AttackAnimationState {
        private int lastTick;
        private float previous;
        private float current;

        private AttackAnimationState(int tick, float initial) {
            this.lastTick = tick;
            this.previous = initial;
            this.current = initial;
        }
    }

    private static final class AnimationClockState {
        private float lastVanillaTime;
        private float temporalTime;

        private AnimationClockState(float initialTime) {
            this.lastVanillaTime = initialTime;
            this.temporalTime = initialTime;
        }
    }

    public static double rateAt(double x, double y, double z) {
        List<TimeDilationFieldSnapshot> fields = activeFields;
        if (fields.isEmpty()) {
            return TimeDilationMath.NORMAL_RATE;
        }
        return TimeDilationFieldSnapshotSampler.sample(fields, x, y, z);
    }

    public static void applyFields(List<TimeDilationFieldSnapshot> fields) {
        activeFields = List.copyOf(fields);
    }
}
