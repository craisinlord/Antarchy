package com.craisinlord.antarchy.neoforge.network;

import com.craisinlord.antarchy.content.network.TimeDilationRatePayload;
import com.craisinlord.antarchy.content.network.TimeDilationFieldsPayload;
import com.craisinlord.antarchy.content.time.TimeDilationFieldSnapshot;
import java.util.List;
import com.craisinlord.antarchy.content.time.TimeDilationApi;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class AntarchyTimeDilationNetworking {
    private AntarchyTimeDilationNetworking() {
    }

    public static void register(PayloadRegistrar registrar) {
        registrar.playToClient(
                TimeDilationRatePayload.TYPE,
                TimeDilationRatePayload.STREAM_CODEC,
                AntarchyTimeDilationNetworking::handleTimeDilationRate
        );
        registrar.playToClient(
                TimeDilationFieldsPayload.TYPE,
                TimeDilationFieldsPayload.STREAM_CODEC,
                AntarchyTimeDilationNetworking::handleTimeDilationFields
        );
    }

    public static void syncRate(Entity entity, double rate) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, new TimeDilationRatePayload(entity.getUUID(), rate));
    }

    public static void syncRateToPlayer(ServerPlayer player, Entity entity, double rate) {
        PacketDistributor.sendToPlayer(player, new TimeDilationRatePayload(entity.getUUID(), rate));
    }

    public static void syncFields(ServerPlayer player, List<TimeDilationFieldSnapshot> fields) {
        PacketDistributor.sendToPlayer(player, new TimeDilationFieldsPayload(fields));
    }

    private static void handleTimeDilationRate(TimeDilationRatePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            TimeDilationApi.applySyncedRate(payload.entityUuid(), payload.rate());
        });
    }

    private static void handleTimeDilationFields(TimeDilationFieldsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> com.craisinlord.antarchy.content.client.ClientTimeDilationTicker.applyFields(payload.fields()));
    }
}
