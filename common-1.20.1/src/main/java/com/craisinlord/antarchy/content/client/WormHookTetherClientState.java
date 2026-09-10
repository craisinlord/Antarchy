package com.craisinlord.antarchy.content.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class WormHookTetherClientState {
    private static final Map<Integer, Integer> TETHERS = new ConcurrentHashMap<>();

    private WormHookTetherClientState() {
    }

    public static void update(int playerId, int hookId) {
        if (hookId < 0) {
            TETHERS.remove(playerId);
        } else {
            TETHERS.put(playerId, hookId);
        }
    }

    public static Map<Integer, Integer> snapshot() {
        return Map.copyOf(TETHERS);
    }

    public static void clear() {
        TETHERS.clear();
    }
}
