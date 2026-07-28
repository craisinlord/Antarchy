package com.craisinlord.antarchy.forge.network;

import com.craisinlord.antarchy.content.client.HerculesBeetleImpactShakeClientState;
import com.craisinlord.antarchy.content.entity.HerculesBeetleEntity;
import com.craisinlord.antarchy.content.network.HerculesBeetleFlightTogglePayload;
import com.craisinlord.antarchy.content.network.HerculesBeetleImpactShakePayload;
import com.craisinlord.antarchy.content.network.HerculesBeetleJumpInputPayload;
import com.craisinlord.antarchy.content.network.HerculesBeetleMountedAttackPayload;
import com.craisinlord.antarchy.content.network.HerculesBeetleMountedChargePayload;
import net.minecraft.server.level.ServerPlayer;

public final class HerculesBeetleNetworking {
    private HerculesBeetleNetworking() {
    }

    public static void register() {
        AntarchyForgeNetworkCore.registerC2S(HerculesBeetleJumpInputPayload.class, HerculesBeetleJumpInputPayload.STREAM_CODEC, HerculesBeetleNetworking::handleJumpInput);
        AntarchyForgeNetworkCore.registerC2S(HerculesBeetleFlightTogglePayload.class, HerculesBeetleFlightTogglePayload.STREAM_CODEC, HerculesBeetleNetworking::handleFlightToggle);
        AntarchyForgeNetworkCore.registerC2S(HerculesBeetleMountedAttackPayload.class, HerculesBeetleMountedAttackPayload.STREAM_CODEC, HerculesBeetleNetworking::handleMountedAttack);
        AntarchyForgeNetworkCore.registerC2S(HerculesBeetleMountedChargePayload.class, HerculesBeetleMountedChargePayload.STREAM_CODEC, HerculesBeetleNetworking::handleMountedCharge);
        AntarchyForgeNetworkCore.registerS2C(HerculesBeetleImpactShakePayload.class, HerculesBeetleImpactShakePayload.STREAM_CODEC, HerculesBeetleNetworking::handleImpactShake);
    }

    private static void handleJumpInput(ServerPlayer player, HerculesBeetleJumpInputPayload payload) {
        if (player.getVehicle() instanceof HerculesBeetleEntity beetle) {
            beetle.setRiderJumpPressed(payload.pressing());
        }
    }

    private static void handleMountedAttack(ServerPlayer player, HerculesBeetleMountedAttackPayload payload) {
        if (player.getVehicle() instanceof HerculesBeetleEntity beetle) {
            beetle.handleMountedRegularAttack(player);
        }
    }

    private static void handleFlightToggle(ServerPlayer player, HerculesBeetleFlightTogglePayload payload) {
        if (player.getVehicle() instanceof HerculesBeetleEntity beetle) {
            beetle.toggleMountedFlight(player);
        }
    }

    private static void handleMountedCharge(ServerPlayer player, HerculesBeetleMountedChargePayload payload) {
        if (player.getVehicle() instanceof HerculesBeetleEntity beetle) {
            if (payload.pressing()) {
                beetle.startMountedCharge(player);
            } else {
                beetle.releaseMountedCharge(player);
            }
        }
    }

    private static void handleImpactShake(HerculesBeetleImpactShakePayload payload) {
        HerculesBeetleImpactShakeClientState.trigger(payload.durationTicks());
    }
}
