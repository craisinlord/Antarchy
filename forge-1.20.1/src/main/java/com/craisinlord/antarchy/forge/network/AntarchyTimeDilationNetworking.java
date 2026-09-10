package com.craisinlord.antarchy.forge.network;

import com.craisinlord.antarchy.content.network.TimeDilationFieldsPayload;
import com.craisinlord.antarchy.content.network.TimeDilationRatePayload;
import com.craisinlord.antarchy.content.time.TimeDilationFieldSnapshot;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public final class AntarchyTimeDilationNetworking {
    private AntarchyTimeDilationNetworking() {}

    public static void register() {
        AntarchyForgeNetworkCore.registerS2C(TimeDilationRatePayload.class, TimeDilationRatePayload.STREAM_CODEC,
                payload -> com.craisinlord.antarchy.content.time.TimeDilationApi.applySyncedRate(payload.entityUuid(), payload.rate()));
        AntarchyForgeNetworkCore.registerS2C(TimeDilationFieldsPayload.class, TimeDilationFieldsPayload.STREAM_CODEC,
                payload -> com.craisinlord.antarchy.content.client.ClientTimeDilationTicker.applyFields(payload.fields()));
    }

    public static void syncRate(Entity entity, double rate) {
        AntarchyForgeNetworkCore.sendToTrackingEntity(entity, new TimeDilationRatePayload(entity.getUUID(), rate));
    }

    public static void syncRateToPlayer(ServerPlayer player, Entity entity, double rate) {
        AntarchyForgeNetworkCore.sendToPlayer(player, new TimeDilationRatePayload(entity.getUUID(), rate));
    }

    public static void syncFields(ServerPlayer player, List<TimeDilationFieldSnapshot> fields) {
        AntarchyForgeNetworkCore.sendToPlayer(player, new TimeDilationFieldsPayload(fields));
    }
}
