package com.craisinlord.antarchy.forge.network;

import com.craisinlord.antarchy.content.entity.DorrieEntity;
import com.craisinlord.antarchy.content.network.DorrieChargeJumpPayload;
import com.craisinlord.antarchy.content.network.DorrieJumpInputPayload;
import net.minecraft.server.level.ServerPlayer;

public final class DorrieJumpNetworking {
    private DorrieJumpNetworking() {}

    public static void register() {
        AntarchyForgeNetworkCore.registerC2S(DorrieJumpInputPayload.class, DorrieJumpInputPayload.STREAM_CODEC, DorrieJumpNetworking::handleJumpInput);
        AntarchyForgeNetworkCore.registerC2S(DorrieChargeJumpPayload.class, DorrieChargeJumpPayload.STREAM_CODEC, DorrieJumpNetworking::handleChargeInput);
    }

    private static void handleJumpInput(ServerPlayer player, DorrieJumpInputPayload payload) {
        if (!(player.getVehicle() instanceof DorrieEntity dorrie)) return;
        dorrie.setPressingJump(payload.pressing());
    }

    private static void handleChargeInput(ServerPlayer player, DorrieChargeJumpPayload payload) {
        if (!(player.getVehicle() instanceof DorrieEntity dorrie)) return;
        if (payload.pressing()) {
            dorrie.startJumpCharge();
        } else {
            dorrie.releaseJump();
        }
    }
}
