package com.craisinlord.antarchy.content.client;

import com.craisinlord.antarchy.content.network.AntmailResultPayload;
import net.minecraft.core.BlockPos;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class AntmailClientState {
    private static final Map<BlockPos, AntmailResultPayload> RESULTS = new ConcurrentHashMap<>();

    private AntmailClientState() {
    }

    public static void update(AntmailResultPayload payload) {
        RESULTS.put(payload.pos(), payload);
    }

    public static AntmailResultPayload get(BlockPos pos) {
        return RESULTS.get(pos);
    }
}
