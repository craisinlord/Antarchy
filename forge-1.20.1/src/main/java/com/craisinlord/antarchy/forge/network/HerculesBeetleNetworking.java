package com.craisinlord.antarchy.forge.network;

import com.craisinlord.antarchy.content.client.HerculesBeetleImpactShakeClientState;
import com.craisinlord.antarchy.content.entity.HerculesBeetleEntity;
import com.craisinlord.antarchy.content.network.HerculesBeetleFlightTogglePayload;
import com.craisinlord.antarchy.content.network.HerculesBeetleImpactShakePayload;
import com.craisinlord.antarchy.content.network.HerculesBeetleJumpInputPayload;
import com.craisinlord.antarchy.content.network.HerculesBeetleMountedAttackPayload;
import com.craisinlord.antarchy.content.network.HerculesBeetleMountedChargePayload;
import net.minecraft.server.level.ServerPlayer;
import com.craisinlord.antarchy.forge.network.AntarchyPayloadContext;
import com.craisinlord.antarchy.forge.network.AntarchyForgePayloadRegistrar;

public final class HerculesBeetleNetworking {
    private HerculesBeetleNetworking() {
    }

    public static void register(AntarchyForgePayloadRegistrar registrar) {
        registrar.playToServer(
                HerculesBeetleJumpInputPayload.TYPE,
                HerculesBeetleJumpInputPayload.STREAM_CODEC,
                HerculesBeetleNetworking::handleJumpInput
        ).playToServer(
                HerculesBeetleFlightTogglePayload.TYPE,
                HerculesBeetleFlightTogglePayload.STREAM_CODEC,
                HerculesBeetleNetworking::handleFlightToggle
        ).playToServer(
                HerculesBeetleMountedAttackPayload.TYPE,
                HerculesBeetleMountedAttackPayload.STREAM_CODEC,
                HerculesBeetleNetworking::handleMountedAttack
        ).playToServer(
                HerculesBeetleMountedChargePayload.TYPE,
                HerculesBeetleMountedChargePayload.STREAM_CODEC,
                HerculesBeetleNetworking::handleMountedCharge
        ).playToClient(
                HerculesBeetleImpactShakePayload.TYPE,
                HerculesBeetleImpactShakePayload.STREAM_CODEC,
                HerculesBeetleNetworking::handleImpactShake
        );
    }

    private static void handleJumpInput(HerculesBeetleJumpInputPayload payload, AntarchyPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            if (player.getVehicle() instanceof HerculesBeetleEntity beetle) {
                beetle.setRiderJumpPressed(payload.pressing());
            }
        });
    }

    private static void handleMountedAttack(HerculesBeetleMountedAttackPayload payload, AntarchyPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            if (player.getVehicle() instanceof HerculesBeetleEntity beetle) {
                beetle.handleMountedRegularAttack(player);
            }
        });
    }

    private static void handleFlightToggle(HerculesBeetleFlightTogglePayload payload, AntarchyPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            if (player.getVehicle() instanceof HerculesBeetleEntity beetle) {
                beetle.toggleMountedFlight(player);
            }
        });
    }

    private static void handleMountedCharge(HerculesBeetleMountedChargePayload payload, AntarchyPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            if (player.getVehicle() instanceof HerculesBeetleEntity beetle) {
                if (payload.pressing()) {
                    beetle.startMountedCharge(player);
                } else {
                    beetle.releaseMountedCharge(player);
                }
            }
        });
    }

    private static void handleImpactShake(HerculesBeetleImpactShakePayload payload, AntarchyPayloadContext context) {
        context.enqueueWork(() -> HerculesBeetleImpactShakeClientState.trigger(payload.durationTicks()));
    }
}
