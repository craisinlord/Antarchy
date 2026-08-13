package com.craisinlord.antarchy.content.boss;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyTags;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class BossCombatUtil {
    private BossCombatUtil() {}

    private static final Map<UUID, MagicWindowState> MAGIC_WINDOWS = new HashMap<>();
    private static final Map<UUID, Long> MAGIC_WARD_EXPIRY_TICK = new HashMap<>();
    private static final Map<UUID, Integer> MAGIC_WARD_BREACH_COUNTS = new HashMap<>();

    private static final class MagicWindowState {
        long windowStartTick;
        float accumulated;
    }

    public static boolean isOutOfDamageRange(Mob boss, double range) {
        if (range <= 0.0D) {
            return false;
        }
        return boss.level().getNearestPlayer(boss, range) == null;
    }

    public static void clampHalfHealthCrossing(LivingEntity boss, float preHitHealth) {
        float halfHealth = boss.getMaxHealth() * 0.5F;
        if (preHitHealth > halfHealth && boss.getHealth() < halfHealth) {
            boss.setHealth(halfHealth);
        }
    }

    public static boolean isMagicBurstSource(DamageSource source) {
        if (source.is(AntarchyTags.DamageType.ANTARCHY_MAGIC_BURST)) {
            return true;
        }
        ResourceLocation typeId = source.typeHolder().unwrapKey().map(ResourceKey::location).orElse(null);
        if (typeId == null) {
            return false;
        }
        for (String namespace : AntarchySettings.magicBurstDamageNamespaces()) {
            if (typeId.getNamespace().equals(namespace)) {
                return true;
            }
        }
        return false;
    }

    public static float trackMagicBurstWindow(LivingEntity boss, float amount, int windowTicks) {
        long now = boss.level().getGameTime();
        MagicWindowState state = MAGIC_WINDOWS.computeIfAbsent(boss.getUUID(), id -> new MagicWindowState());
        if (now - state.windowStartTick >= windowTicks) {
            state.windowStartTick = now;
            state.accumulated = 0.0F;
        }
        state.accumulated += amount;
        return state.accumulated;
    }

    public static float remainingWindowBudget(LivingEntity boss, float windowCap) {
        MagicWindowState state = MAGIC_WINDOWS.get(boss.getUUID());
        if (state == null) {
            return windowCap;
        }
        long now = boss.level().getGameTime();
        if (now - state.windowStartTick >= AntarchySettings.bossMagicWindowTicks()) {
            return windowCap;
        }
        return Math.max(0.0F, windowCap - state.accumulated);
    }

    public static boolean isMagicWarded(LivingEntity boss) {
        Long expiryTick = MAGIC_WARD_EXPIRY_TICK.get(boss.getUUID());
        return expiryTick != null && boss.level().getGameTime() < expiryTick;
    }

    public static void triggerMagicWard(LivingEntity boss, int durationTicks) {
        MAGIC_WARD_EXPIRY_TICK.put(boss.getUUID(), boss.level().getGameTime() + durationTicks);
        MAGIC_WARD_BREACH_COUNTS.remove(boss.getUUID());
    }

    public static int recordMagicWindowBreach(LivingEntity boss) {
        return MAGIC_WARD_BREACH_COUNTS.merge(boss.getUUID(), 1, Integer::sum);
    }
}
