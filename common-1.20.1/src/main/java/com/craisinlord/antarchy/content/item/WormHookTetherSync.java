package com.craisinlord.antarchy.content.item;

import java.util.function.BiConsumer;
import net.minecraft.server.level.ServerPlayer;

public final class WormHookTetherSync {
    private static final BiConsumer<ServerPlayer, Integer> NOOP = (player, hookId) -> {
    };

    private static volatile BiConsumer<ServerPlayer, Integer> sink = NOOP;

    private WormHookTetherSync() {
    }

    public static void setSink(BiConsumer<ServerPlayer, Integer> sink) {
        WormHookTetherSync.sink = sink == null ? NOOP : sink;
    }

    public static void send(ServerPlayer player, int hookId) {
        sink.accept(player, hookId);
    }
}
