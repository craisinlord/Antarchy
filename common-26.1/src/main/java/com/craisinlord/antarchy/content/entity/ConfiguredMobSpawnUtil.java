package com.craisinlord.antarchy.content.entity;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class ConfiguredMobSpawnUtil {
    private ConfiguredMobSpawnUtil() {
    }

    public static void applyConfiguredHealth(Mob mob, double maxHealth) {
        AttributeInstance maxHealthAttribute = mob.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttribute != null) {
            maxHealthAttribute.setBaseValue(maxHealth);
        }
        mob.setHealth((float) maxHealth);
    }
}
