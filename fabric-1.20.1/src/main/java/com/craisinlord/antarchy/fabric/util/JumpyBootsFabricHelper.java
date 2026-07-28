package com.craisinlord.antarchy.fabric.util;

import net.minecraft.world.entity.LivingEntity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class JumpyBootsFabricHelper {
    private static final Map<UUID, Long> PROTECTION_TIMES = new ConcurrentHashMap<>();

    public static long getProtectionUntil(LivingEntity entity) {
        return PROTECTION_TIMES.getOrDefault(entity.getUUID(), 0L);
    }

    public static void setProtectionUntil(LivingEntity entity, long gameTime) {
        PROTECTION_TIMES.put(entity.getUUID(), gameTime);
    }
}
