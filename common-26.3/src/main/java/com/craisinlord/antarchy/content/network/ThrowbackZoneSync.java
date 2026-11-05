package com.craisinlord.antarchy.content.network;

import java.util.function.BiConsumer;
import net.minecraft.server.level.ServerPlayer;

public final class ThrowbackZoneSync {
    private static final BiConsumer<ServerPlayer, ThrowbackZonePayload> NOOP = (player, payload) -> {
    };

    private static volatile BiConsumer<ServerPlayer, ThrowbackZonePayload> sink = NOOP;

    private ThrowbackZoneSync() {
    }

    public static void setSink(BiConsumer<ServerPlayer, ThrowbackZonePayload> sink) {
        ThrowbackZoneSync.sink = sink == null ? NOOP : sink;
    }

    public static void send(ServerPlayer player, float strength) {
        sink.accept(player, new ThrowbackZonePayload(strength));
    }
}
