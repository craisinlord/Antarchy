package com.craisinlord.antarchy.content.network;

import java.util.function.BiConsumer;
import net.minecraft.server.level.ServerPlayer;

public final class AntTransitSync {
    private static final BiConsumer<ServerPlayer, AntTransitPayload> NOOP = (player, payload) -> {
    };

    private static volatile BiConsumer<ServerPlayer, AntTransitPayload> sink = NOOP;

    private AntTransitSync() {
    }

    public static void setSink(BiConsumer<ServerPlayer, AntTransitPayload> sink) {
        AntTransitSync.sink = sink == null ? NOOP : sink;
    }

    public static void send(ServerPlayer player, AntTransitPayload payload) {
        sink.accept(player, payload);
    }

    public static void sendToSelfAndTracking(ServerPlayer player, AntTransitPayload payload) {
        send(player, payload);
        for (ServerPlayer other : player.serverLevel().players()) {
            if (other != player && other.distanceToSqr(player) < 64.0D * 64.0D) {
                send(other, payload);
            }
        }
    }
}
