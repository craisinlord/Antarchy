package com.craisinlord.antarchy.content.block.entity;

import net.minecraft.world.entity.MobSpawnType;

public final class UndertrialSpawnContext implements AutoCloseable {
    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

    private boolean closed;

    private UndertrialSpawnContext() {
        DEPTH.set(DEPTH.get() + 1);
    }

    public static UndertrialSpawnContext enter() {
        return new UndertrialSpawnContext();
    }

    public static boolean isActive() {
        return DEPTH.get() > 0;
    }

    public static boolean isTrialSpawn(MobSpawnType spawnReason) {
        return spawnReason == MobSpawnType.SPAWNER && isActive();
    }

    @Override
    public void close() {
        if (!closed) {
            closed = true;
            int depth = DEPTH.get() - 1;
            if (depth <= 0) {
                DEPTH.remove();
            } else {
                DEPTH.set(depth);
            }
        }
    }
}
