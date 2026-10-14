package com.craisinlord.antarchy.forge.network;

import com.craisinlord.antarchy.content.entity.DorrieEntity;
import com.craisinlord.antarchy.content.network.DorrieChargeJumpPayload;
import com.craisinlord.antarchy.content.network.DorrieJumpInputPayload;
import net.minecraft.server.level.ServerPlayer;
import com.craisinlord.antarchy.forge.network.AntarchyPayloadContext;
import com.craisinlord.antarchy.forge.network.AntarchyForgePayloadRegistrar;

public final class DorrieJumpNetworking {
    private DorrieJumpNetworking() {}

    public static void register(AntarchyForgePayloadRegistrar registrar) {
        registrar.playToServer(
                DorrieJumpInputPayload.TYPE,
                DorrieJumpInputPayload.STREAM_CODEC,
                DorrieJumpNetworking::handleJumpInput
        );
        registrar.playToServer(
                DorrieChargeJumpPayload.TYPE,
                DorrieChargeJumpPayload.STREAM_CODEC,
                DorrieJumpNetworking::handleChargeInput
        );
    }

    private static void handleJumpInput(DorrieJumpInputPayload payload, AntarchyPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (!(player.getVehicle() instanceof DorrieEntity dorrie)) return;
            dorrie.setPressingJump(payload.pressing());
        });
    }

    private static void handleChargeInput(DorrieChargeJumpPayload payload, AntarchyPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (!(player.getVehicle() instanceof DorrieEntity dorrie)) return;
            if (payload.pressing()) {
                dorrie.startJumpCharge();
            } else {
                dorrie.releaseJump();
            }
        });
    }
}
