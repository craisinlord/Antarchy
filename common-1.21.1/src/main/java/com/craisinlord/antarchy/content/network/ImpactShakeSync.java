package com.craisinlord.antarchy.content.network;

import java.util.function.BiConsumer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

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

    public static void sendNearby(ServerLevel level, Vec3 pos, float intensity, int durationTicks, float radius) {
        ImpactShakePayload payload = new ImpactShakePayload(pos.x, pos.y, pos.z, intensity, durationTicks, radius);
        double radiusSqr = radius * radius;
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(pos.x, pos.y, pos.z) <= radiusSqr) {
                send(player, payload);
            }
        }
    }
}
