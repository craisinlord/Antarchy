package com.craisinlord.antarchy.content.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class TigerEyeCamouflageClientState {
    public record CamouflageState(boolean active, int blockStateId) {}

    private static final Map<Integer, CamouflageState> STATES = new ConcurrentHashMap<>();

    private TigerEyeCamouflageClientState() {
    }

    public static void update(int entityId, boolean active, int blockStateId) {
        if (!active) {
            STATES.remove(entityId);
            return;
        }
        STATES.put(entityId, new CamouflageState(true, blockStateId));
    }

    public static CamouflageState get(int entityId) {
        return STATES.get(entityId);
    }

    public static void clear() {
        STATES.clear();
    }
}
