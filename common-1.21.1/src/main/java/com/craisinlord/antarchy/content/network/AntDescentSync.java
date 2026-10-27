package com.craisinlord.antarchy.content.network;

import java.util.function.BiConsumer;
import net.minecraft.server.level.ServerPlayer;

public final class AntDescentSync {
    private static final BiConsumer<ServerPlayer, AntDescentPayload> NOOP = (player, payload) -> {
    };

    private static volatile BiConsumer<ServerPlayer, AntDescentPayload> sink = NOOP;

    private AntDescentSync() {
    }

    public static void setSink(BiConsumer<ServerPlayer, AntDescentPayload> sink) {
        AntDescentSync.sink = sink == null ? NOOP : sink;
    }

    public static void send(ServerPlayer player, AntDescentPayload payload) {
        sink.accept(player, payload);
    }

    public static void sendToSelfAndTracking(ServerPlayer player, AntDescentPayload payload) {
        send(player, payload);
        for (ServerPlayer other : player.serverLevel().players()) {
            if (other != player && other.distanceToSqr(player) < 64.0D * 64.0D) {
                send(other, payload);
            }
        }
    }
}
