package com.craisinlord.antarchy.neoforge.network;

import com.craisinlord.antarchy.content.entity.DorrieEntity;
import com.craisinlord.antarchy.content.network.DorrieChargeJumpPayload;
import com.craisinlord.antarchy.content.network.DorrieJumpInputPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class DorrieJumpNetworking {
    private DorrieJumpNetworking() {}

    public static void register(PayloadRegistrar registrar) {
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

    private static void handleJumpInput(DorrieJumpInputPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (!(player.getVehicle() instanceof DorrieEntity dorrie)) return;
            dorrie.setPressingJump(payload.pressing());
        });
    }

    private static void handleChargeInput(DorrieChargeJumpPayload payload, IPayloadContext context) {
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
