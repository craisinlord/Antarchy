package com.craisinlord.antarchy.neoforge.network;

import com.craisinlord.antarchy.content.client.ThoraxisWeatherClientState;
import com.craisinlord.antarchy.content.weather.ThoraxisWeatherKind;
import com.craisinlord.antarchy.content.weather.ThoraxisWeatherSnapshot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ThoraxisWeatherNetworking {
    private ThoraxisWeatherNetworking() {
    }

    public static void register(PayloadRegistrar registrar) {
        registrar.playToClient(
                com.craisinlord.antarchy.content.network.ThoraxisWeatherPayload.TYPE,
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
            PacketDistributor.sendToPlayer(player, payload);
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
        PacketDistributor.sendToPlayer(player, payload);
    }

    private static void handleWeatherPayload(com.craisinlord.antarchy.content.network.ThoraxisWeatherPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ThoraxisWeatherClientState.apply(
                ResourceLocation.parse(payload.dimensionId()),
                ThoraxisWeatherKind.byId(payload.weatherId()),
                payload.expiresAt(),
                payload.anchorX(),
                payload.anchorY(),
                payload.anchorZ()
        ));
    }
}
