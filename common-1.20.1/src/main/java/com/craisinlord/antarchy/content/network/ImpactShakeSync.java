package com.craisinlord.antarchy.content.network;

import java.util.function.BiConsumer;
import net.minecraft.server.level.ServerPlayer;

public final class ImpactShakeSync {
    private static final BiConsumer<ServerPlayer, ImpactShakePayload> NOOP = (player, payload) -> {
    };
    private static volatile BiConsumer<ServerPlayer, ImpactShakePayload> sink = NOOP;

    private ImpactShakeSync() {
    }

    public static void setSink(BiConsumer<ServerPlayer, ImpactShakePayload> sink) {
        ImpactShakeSync.sink = sink == null ? NOOP : sink;
    }

    public static void send(ServerPlayer player, ImpactShakePayload payload) {
        sink.accept(player, payload);
    }
}
