package com.craisinlord.antarchy.content.client;

import com.craisinlord.antarchy.content.network.ComputerAccessResultPayload;
import net.minecraft.core.BlockPos;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ComputerAccessClientState {
    private static final Map<BlockPos, ComputerAccessResultPayload> RESULTS = new ConcurrentHashMap<>();

    private ComputerAccessClientState() {
    }

    public static void update(ComputerAccessResultPayload result) {
        RESULTS.put(result.pos(), result);
        ComputerFileSystemClientState.update(result);
    }

    public static ComputerAccessResultPayload get(BlockPos pos) {
        return RESULTS.get(pos);
    }
}
