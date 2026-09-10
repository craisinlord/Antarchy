package com.craisinlord.antarchy.fabric.network;

import com.craisinlord.antarchy.content.network.TimeDilationFieldsPayload;
import com.craisinlord.antarchy.content.network.TimeDilationRatePayload;
import com.craisinlord.antarchy.content.time.TimeDilationFieldSnapshot;
import com.craisinlord.antarchy.content.time.TimeDilationApi;
import java.util.List;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public final class AntarchyFabricTimeDilationNetworking {
    private AntarchyFabricTimeDilationNetworking() {}

    public static void syncRate(Entity entity, double rate) {
        TimeDilationRatePayload payload = new TimeDilationRatePayload(entity.getUUID(), rate);
        for (ServerPlayer player : PlayerLookup.tracking(entity)) {
            AntarchyFabricNetworking.sendToPlayer(player, payload, TimeDilationRatePayload.STREAM_CODEC, TimeDilationRatePayload.TYPE);
        }
        if (entity instanceof ServerPlayer player) {
            AntarchyFabricNetworking.sendToPlayer(player, payload, TimeDilationRatePayload.STREAM_CODEC, TimeDilationRatePayload.TYPE);
        }
    }

    public static void syncRateToPlayer(ServerPlayer player, Entity entity, double rate) {
        TimeDilationRatePayload payload = new TimeDilationRatePayload(entity.getUUID(), rate);
        AntarchyFabricNetworking.sendToPlayer(player, payload, TimeDilationRatePayload.STREAM_CODEC, TimeDilationRatePayload.TYPE);
    }

    public static void syncFields(ServerPlayer player, List<TimeDilationFieldSnapshot> fields) {
        AntarchyFabricNetworking.sendToPlayer(player, new TimeDilationFieldsPayload(fields),
                TimeDilationFieldsPayload.STREAM_CODEC, TimeDilationFieldsPayload.TYPE);
    }

    public static void handleRate(Entity ignored, TimeDilationRatePayload payload) {
        TimeDilationApi.applySyncedRate(payload.entityUuid(), payload.rate());
    }
}
