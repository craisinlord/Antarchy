package com.craisinlord.antarchy.content.network;

import java.util.function.BiConsumer;
import net.minecraft.server.level.ServerPlayer;

public final class ElythiaEventSync {
    private static final BiConsumer<ServerPlayer, ElythiaEventPayload> NOOP = (player, payload) -> {
    };

    private static volatile BiConsumer<ServerPlayer, ElythiaEventPayload> sink = NOOP;

    private ElythiaEventSync() {
    }

    public static void setSink(BiConsumer<ServerPlayer, ElythiaEventPayload> sink) {
        ElythiaEventSync.sink = sink == null ? NOOP : sink;
    }

    public static void send(ServerPlayer player, ElythiaEventPayload payload) {
        sink.accept(player, payload);
    }
}
