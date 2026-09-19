package com.craisinlord.antarchy.content.block.entity;

/**
 * Identifies spawn-rule checks made by an Undertrial spawner's vanilla
 * TrialSpawner tick. The context is scoped to the current server-thread call
 * so ordinary trial spawners and all other spawn paths retain vanilla rules.
 */
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
