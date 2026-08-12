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

    public static void clampHalfHealthCrossing(LivingEntity boss, float preHitHealth) {
        float halfHealth = boss.getMaxHealth() * 0.5F;
        if (preHitHealth > halfHealth && boss.getHealth() < halfHealth) {
            boss.setHealth(halfHealth);
        }
    }
}
