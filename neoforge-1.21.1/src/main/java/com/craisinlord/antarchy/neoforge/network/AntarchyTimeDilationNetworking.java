package com.craisinlord.antarchy.neoforge.network;

import com.craisinlord.antarchy.content.network.TimeDilationRatePayload;
import com.craisinlord.antarchy.content.time.TimeDilationApi;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
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
    }

    public static void syncRate(ServerPlayer player, double rate) {
        PacketDistributor.sendToPlayer(player, new TimeDilationRatePayload(player.getId(), rate));
    }

    private static void handleTimeDilationRate(TimeDilationRatePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Entity entity = context.player().level().getEntity(payload.entityId());
            if (entity == null && context.player().getId() == payload.entityId()) {
                entity = context.player();
            }
            if (entity != null) {
                TimeDilationApi.applySyncedRate(entity, payload.rate());
            }
        });
    }
}
