package com.craisinlord.antarchy.content.network;

import java.util.function.BiConsumer;
import net.minecraft.server.level.ServerPlayer;

public final class HerculesBeetleImpactShakeSync {
    private static final BiConsumer<ServerPlayer, Integer> NOOP = (player, ticks) -> {
    };

    private static volatile BiConsumer<ServerPlayer, Integer> sink = NOOP;

    private HerculesBeetleImpactShakeSync() {
    }

    public static void setSink(BiConsumer<ServerPlayer, Integer> sink) {
        HerculesBeetleImpactShakeSync.sink = sink == null ? NOOP : sink;
    }

    public static void send(ServerPlayer player, int ticks) {
        sink.accept(player, ticks);
    }
}
