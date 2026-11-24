package com.craisinlord.antarchy.content.block.entity;

import net.minecraft.core.BlockPos;

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

    public static double ceilingFeetY(BlockPos pos, double y, double height) {
        return pos.getY() + 1.0D - 1.0E-3D - height - Math.max(0.0D, y - pos.getY());
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
