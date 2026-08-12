package com.craisinlord.antarchy.content.boss;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public final class BossCombatUtil {
    private BossCombatUtil() {}

    public static boolean isOutOfDamageRange(Mob boss, double range) {
        if (range <= 0.0D) {
            return false;
        }
        return boss.level().getNearestPlayer(boss, range) == null;
    }

    /**
     * Caps a single hit so it cannot bring the boss from above half of its max health
     * down past that threshold in one shot. Once the boss is at or below half health,
     * hits are no longer capped.
     */
    public static float capSingleHitAtHalfHealth(LivingEntity boss, float amount) {
        float halfHealth = boss.getMaxHealth() * 0.5F;
        float currentHealth = boss.getHealth();
        if (currentHealth > halfHealth && currentHealth - amount < halfHealth) {
            return currentHealth - halfHealth;
        }
        return amount;
    }
}
