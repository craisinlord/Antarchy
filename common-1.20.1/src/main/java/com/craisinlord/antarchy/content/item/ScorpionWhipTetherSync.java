package com.craisinlord.antarchy.content.item;

import java.util.function.BiConsumer;
import net.minecraft.server.level.ServerPlayer;

public final class ScorpionWhipTetherSync {
    private static final BiConsumer<ServerPlayer, Integer> NOOP = (player, targetId) -> {
    };

    private static volatile BiConsumer<ServerPlayer, Integer> sink = NOOP;

    private ScorpionWhipTetherSync() {
    }

    public static void setSink(BiConsumer<ServerPlayer, Integer> sink) {
        ScorpionWhipTetherSync.sink = sink == null ? NOOP : sink;
    }

    public static void send(ServerPlayer player, int targetId) {
        sink.accept(player, targetId);
    }
}
