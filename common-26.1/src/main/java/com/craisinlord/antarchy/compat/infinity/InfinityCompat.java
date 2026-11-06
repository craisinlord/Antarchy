package com.craisinlord.antarchy.compat.infinity;

public final class InfinityCompat {
    private static InfinityCompatBridge bridge = InfinityCompatBridge.NOOP;

    private InfinityCompat() {
    }

    public static InfinityCompatBridge get() {
        return bridge;
    }

    public static void bind(InfinityCompatBridge compatBridge) {
        bridge = compatBridge != null ? compatBridge : InfinityCompatBridge.NOOP;
    }
}
