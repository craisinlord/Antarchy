package com.craisinlord.antarchy.content.client;

import com.craisinlord.antarchy.content.network.ComputerAccessPayload;
import com.craisinlord.antarchy.content.network.ComputerAccessResultPayload;
import net.minecraft.core.BlockPos;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ComputerStructureLocatorClientState {
    private static final Map<BlockPos, String> RESULTS = new ConcurrentHashMap<>();

    private ComputerStructureLocatorClientState() {
    }

    public static void searching(BlockPos pos, String dimensionId) {
        String dimension = dimensionId == null || dimensionId.isBlank() ? "TARGET DIMENSION" : dimensionId.toUpperCase(java.util.Locale.ROOT);
        RESULTS.put(pos, "SEARCHING IN " + dimension + "...");
    }

    public static void update(ComputerAccessResultPayload payload) {
        String[] envelope = payload.data().split("\u0000", 3);
        if (envelope.length != 3) return;
        try {
            if (Integer.parseInt(envelope[0]) != ComputerAccessPayload.LOCATE_STRUCTURE) return;
        } catch (NumberFormatException ignored) {
            return;
        }

        String message = switch (envelope[1]) {
            case "not_found" -> "NO STRUCTURE FOUND WITHIN RANGE";
            case "dimension_unavailable" -> "TARGET DIMENSION UNAVAILABLE";
            default -> "LOCATOR UNAVAILABLE";
        };
        RESULTS.put(payload.pos(), payload.result() == ComputerAccessResultPayload.SUCCESS
                ? "STRUCTURE START // " + envelope[2]
                : message);
    }

    public static String get(BlockPos pos) {
        return RESULTS.getOrDefault(pos, "COORDINATES // SEARCH WHEN SELECTED");
    }
}
