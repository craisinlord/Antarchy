package com.craisinlord.antarchy.content.network;

import java.util.function.BiConsumer;
import net.minecraft.server.level.ServerPlayer;

public final class ElythiaEchoTestSync {
    private static final BiConsumer<ServerPlayer, ElythiaEchoTestPayload> NOOP = (player, payload) -> {
    };

    private static volatile BiConsumer<ServerPlayer, ElythiaEchoTestPayload> sink = NOOP;

    private ElythiaEchoTestSync() {
    }

    public static void setSink(BiConsumer<ServerPlayer, ElythiaEchoTestPayload> sink) {
        ElythiaEchoTestSync.sink = sink == null ? NOOP : sink;
    }

    public static void send(ServerPlayer player, boolean immediate) {
        sink.accept(player, new ElythiaEchoTestPayload(immediate));
    }
}
