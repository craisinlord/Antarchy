package com.craisinlord.antarchy.content.client;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class BloodCrystalKatanaTrailClientState {
    private static final Map<Integer, Integer> ACTIVE = new HashMap<>();

    private BloodCrystalKatanaTrailClientState() {}

    public static void trigger(int entityId, int durationTicks) {
        if (durationTicks <= 0) {
            ACTIVE.remove(entityId);
            return;
        }
        ACTIVE.put(entityId, durationTicks);
    }

    public static int getRemainingTicks(int entityId) {
        return ACTIVE.getOrDefault(entityId, 0);
    }

    public static void tick() {
        Iterator<Map.Entry<Integer, Integer>> iterator = ACTIVE.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Integer> entry = iterator.next();
            int remaining = entry.getValue() - 1;
            if (remaining <= 0) {
                iterator.remove();
            } else {
                entry.setValue(remaining);
            }
        }
    }

    public static void clear() {
        ACTIVE.clear();
    }
}
