package com.craisinlord.antarchy.content.menu;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class RoyalJudgmentState {
    private static final Map<UUID, Set<Integer>> SEALED_SLOTS = new HashMap<>();

    private RoyalJudgmentState() {
    }

    public static void seal(UUID playerId, Set<Integer> slots) {
        SEALED_SLOTS.put(playerId, Set.copyOf(slots));
    }

    public static boolean isSealed(UUID playerId, int inventorySlot) {
        return SEALED_SLOTS.getOrDefault(playerId, Set.of()).contains(inventorySlot);
    }

    public static void clear(UUID playerId) {
        SEALED_SLOTS.remove(playerId);
    }
}
