package com.craisinlord.antarchy.fabric.network;

import com.craisinlord.antarchy.content.network.TimeDilationRatePayload;
import com.craisinlord.antarchy.content.network.TimeDilationFieldsPayload;
import com.craisinlord.antarchy.content.time.TimeDilationApi;
import com.craisinlord.antarchy.content.time.TimeDilationFieldSnapshot;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import java.util.List;

public final class AntarchyFabricTimeDilationNetworking {
    private AntarchyFabricTimeDilationNetworking() {
    }

    public static void registerPayloadTypes() {
        PayloadTypeRegistry.playS2C().register(TimeDilationRatePayload.TYPE, TimeDilationRatePayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(TimeDilationFieldsPayload.TYPE, TimeDilationFieldsPayload.STREAM_CODEC);
    }

    public static void registerClientReceiver() {
        ClientPlayNetworking.registerGlobalReceiver(TimeDilationRatePayload.TYPE, (payload, context) ->
                context.client().execute(() -> handleRate(context.player(), payload)));
        ClientPlayNetworking.registerGlobalReceiver(TimeDilationFieldsPayload.TYPE, (payload, context) ->
                context.client().execute(() -> com.craisinlord.antarchy.content.client.ClientTimeDilationTicker.applyFields(payload.fields())));
    }

    public static void syncRate(Entity entity, double rate) {
        TimeDilationRatePayload payload = new TimeDilationRatePayload(entity.getUUID(), rate);
        for (ServerPlayer player : PlayerLookup.tracking(entity)) {
            ServerPlayNetworking.send(player, payload);
        }
        if (entity instanceof ServerPlayer player) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    public static void syncRateToPlayer(ServerPlayer player, Entity entity, double rate) {
        ServerPlayNetworking.send(player, new TimeDilationRatePayload(entity.getUUID(), rate));
    }

    public static void syncFields(ServerPlayer player, List<TimeDilationFieldSnapshot> fields) {
        ServerPlayNetworking.send(player, new TimeDilationFieldsPayload(fields));
    }

    private static void handleRate(Entity contextPlayer, TimeDilationRatePayload payload) {
        TimeDilationApi.applySyncedRate(payload.entityUuid(), payload.rate());
    }
}
