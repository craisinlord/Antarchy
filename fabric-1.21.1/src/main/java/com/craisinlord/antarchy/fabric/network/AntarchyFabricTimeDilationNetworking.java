package com.craisinlord.antarchy.fabric.network;

import com.craisinlord.antarchy.content.network.TimeDilationRatePayload;
import com.craisinlord.antarchy.content.time.TimeDilationApi;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public final class AntarchyFabricTimeDilationNetworking {
    private AntarchyFabricTimeDilationNetworking() {
    }

    public static void registerPayloadTypes() {
        PayloadTypeRegistry.playS2C().register(TimeDilationRatePayload.TYPE, TimeDilationRatePayload.STREAM_CODEC);
    }

    public static void registerClientReceiver() {
        ClientPlayNetworking.registerGlobalReceiver(TimeDilationRatePayload.TYPE, (payload, context) ->
                context.client().execute(() -> handleRate(context.player(), payload)));
    }

    public static void syncRate(ServerPlayer player, double rate) {
        ServerPlayNetworking.send(player, new TimeDilationRatePayload(player.getId(), rate));
    }

    private static void handleRate(Entity contextPlayer, TimeDilationRatePayload payload) {
        Entity entity = contextPlayer.level().getEntity(payload.entityId());
        if (entity == null && contextPlayer.getId() == payload.entityId()) {
            entity = contextPlayer;
        }
        if (entity != null) {
            TimeDilationApi.applySyncedRate(entity, payload.rate());
        }
    }
}
