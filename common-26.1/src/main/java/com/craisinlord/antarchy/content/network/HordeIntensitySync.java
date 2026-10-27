package com.craisinlord.antarchy.content.network;

import java.util.function.BiConsumer;
import net.minecraft.server.level.ServerPlayer;

public final class HordeIntensitySync {
    private static final BiConsumer<ServerPlayer, HordeIntensityPayload> NOOP = (player, payload) -> {
    };

    private static volatile BiConsumer<ServerPlayer, HordeIntensityPayload> sink = NOOP;

    private HordeIntensitySync() {
    }

    public static void setSink(BiConsumer<ServerPlayer, HordeIntensityPayload> sink) {
        HordeIntensitySync.sink = sink == null ? NOOP : sink;
    }

    public static void send(ServerPlayer player, float intensity) {
        sink.accept(player, new HordeIntensityPayload(intensity));
    }
}
