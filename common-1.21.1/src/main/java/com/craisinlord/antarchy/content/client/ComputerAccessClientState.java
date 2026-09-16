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
        if (result.data().startsWith("18\u0000") || result.data().startsWith("19\u0000")) {
            BlockleClientState.update(result);
        }
        // File, terminal, desktop, and wallpaper responses carry their own operation
        // status. They must not replace the last authoritative login state.
        if (result.data().isBlank()) RESULTS.put(result.pos(), result);
        ComputerFileSystemClientState.update(result);
    }

    public static void clear(BlockPos pos) {
        RESULTS.remove(pos);
    }

    public static ComputerAccessResultPayload get(BlockPos pos) {
        return RESULTS.get(pos);
    }
}
