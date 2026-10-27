package com.craisinlord.antarchy.content.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ScorpionWhipTetherClientState {
    private static final Map<Integer, Integer> TETHERS = new ConcurrentHashMap<>();

    private ScorpionWhipTetherClientState() {
    }

    public static void update(int playerId, int targetId) {
        if (targetId < 0) {
            TETHERS.remove(playerId);
        } else {
            TETHERS.put(playerId, targetId);
        }
    }

    public static Map<Integer, Integer> snapshot() {
        return Map.copyOf(TETHERS);
    }

    public static void clear() {
        TETHERS.clear();
    }
}
