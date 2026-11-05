package com.craisinlord.antarchy.content.network;

import java.util.function.BiConsumer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public final class ThoraxisEventSync {
    private static final BiConsumer<ServerPlayer, CustomPacketPayload> NOOP = (player, payload) -> {
    };

    private static volatile BiConsumer<ServerPlayer, CustomPacketPayload> sink = NOOP;

    private ThoraxisEventSync() {
    }

    public static void setSink(BiConsumer<ServerPlayer, CustomPacketPayload> sink) {
        ThoraxisEventSync.sink = sink == null ? NOOP : sink;
    }

    public static void send(ServerPlayer player, CustomPacketPayload payload) {
        sink.accept(player, payload);
    }
}
