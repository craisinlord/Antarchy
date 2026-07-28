package com.craisinlord.antarchy.forge.network;

import com.craisinlord.antarchy.content.client.ThoraxisWeatherClientState;
import com.craisinlord.antarchy.content.weather.ThoraxisWeatherKind;
import com.craisinlord.antarchy.content.weather.ThoraxisWeatherSnapshot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class ThoraxisWeatherNetworking {
    private ThoraxisWeatherNetworking() {
    }

    public static void register() {
        AntarchyForgeNetworkCore.registerS2C(
                com.craisinlord.antarchy.content.network.ThoraxisWeatherPayload.class,
                com.craisinlord.antarchy.content.network.ThoraxisWeatherPayload.STREAM_CODEC,
                ThoraxisWeatherNetworking::handleWeatherPayload
        );
    }

    public static void syncLevel(ServerLevel level, ThoraxisWeatherSnapshot snapshot) {
        com.craisinlord.antarchy.content.network.ThoraxisWeatherPayload payload = new com.craisinlord.antarchy.content.network.ThoraxisWeatherPayload(
                snapshot.dimension(),
                snapshot.kind(),
                snapshot.expiresAt(),
                snapshot.anchor().getX(),
                snapshot.anchor().getY(),
                snapshot.anchor().getZ()
        );

        for (ServerPlayer player : level.players()) {
            AntarchyForgeNetworkCore.sendToPlayer(player, payload);
        }
    }

    public static void syncPlayer(ServerPlayer player, ThoraxisWeatherSnapshot snapshot) {
        com.craisinlord.antarchy.content.network.ThoraxisWeatherPayload payload = new com.craisinlord.antarchy.content.network.ThoraxisWeatherPayload(
                snapshot.dimension(),
                snapshot.kind(),
                snapshot.expiresAt(),
                snapshot.anchor().getX(),
                snapshot.anchor().getY(),
                snapshot.anchor().getZ()
        );
        AntarchyForgeNetworkCore.sendToPlayer(player, payload);
    }

    private static void handleWeatherPayload(com.craisinlord.antarchy.content.network.ThoraxisWeatherPayload payload) {
        ThoraxisWeatherClientState.apply(
                new ResourceLocation(payload.dimensionId()),
                ThoraxisWeatherKind.byId(payload.weatherId()),
                payload.expiresAt(),
                payload.anchorX(),
                payload.anchorY(),
                payload.anchorZ()
        );
    }
}
