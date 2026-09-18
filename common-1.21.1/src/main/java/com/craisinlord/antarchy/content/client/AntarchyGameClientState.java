package com.craisinlord.antarchy.content.client;

import com.craisinlord.antarchy.content.network.AntarchyGamePayload;
import com.craisinlord.antarchy.content.network.AntarchyGameResultPayload;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;

/** Client state for Antarchy-owned games hosted by AntOS. */
public final class AntarchyGameClientState {
    private static final Map<String, String> GAME_STATES = new ConcurrentHashMap<>();

    private AntarchyGameClientState() { }

    public static void update(AntarchyGameResultPayload result) {
        String[] envelope = result.data().split("\u0000", 3);
        if (envelope.length < 3) return;
        if (envelope[0].equals(Integer.toString(AntarchyGamePayload.BASILISK_STATE))
                || envelope[0].equals(Integer.toString(AntarchyGamePayload.ANTMAN_STATE))) {
            GAME_STATES.put(key(result.pos(), envelope[0]),
                    result.success() ? envelope[2] : "");
        }
    }

    public static String getGameState(BlockPos pos, int action) { return GAME_STATES.get(key(pos, Integer.toString(action))); }
    public static void clearGameState(BlockPos pos, int action) { GAME_STATES.remove(key(pos, Integer.toString(action))); }
    private static String key(BlockPos pos, String action) { return pos.asLong() + "|" + action; }
}
