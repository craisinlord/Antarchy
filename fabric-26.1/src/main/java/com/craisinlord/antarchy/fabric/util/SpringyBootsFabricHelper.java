package com.craisinlord.antarchy.fabric.util;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.entity.LivingEntity;

public class SpringyBootsFabricHelper {
    private static final Map<UUID, Long> PROTECTION_TIMES = new ConcurrentHashMap<>();

    public static long getProtectionUntil(LivingEntity entity) {
        return PROTECTION_TIMES.getOrDefault(entity.getUUID(), 0L);
    }

    public static void setProtectionUntil(LivingEntity entity, long gameTime) {
        PROTECTION_TIMES.put(entity.getUUID(), gameTime);
    }
}
