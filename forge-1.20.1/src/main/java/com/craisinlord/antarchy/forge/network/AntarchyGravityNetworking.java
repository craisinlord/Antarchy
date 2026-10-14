package com.craisinlord.antarchy.forge.network;

import com.craisinlord.antarchy.content.entity.DiamondMinecartEntity;
import com.craisinlord.antarchy.content.item.BigBerthaItem;
import com.craisinlord.antarchy.content.item.EyeOfTheStormItem;
import com.craisinlord.antarchy.content.item.GravityGunItem;
import com.craisinlord.antarchy.content.item.PortalGunItem;
import com.craisinlord.antarchy.content.network.BigBerthaModeCyclePayload;
import com.craisinlord.antarchy.content.network.RoyalGuardianSwordModeCyclePayload;
import com.craisinlord.antarchy.content.network.EyeOfStormPrimaryPayload;
import com.craisinlord.antarchy.content.network.GravityGunPrimaryPayload;
import com.craisinlord.antarchy.content.network.GravityGunScrollPayload;
import com.craisinlord.antarchy.content.network.TemporalTunerScrollPayload;
import com.craisinlord.antarchy.content.item.TemporalTunerItem;
import com.craisinlord.antarchy.content.network.PortalGunPrimaryPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import com.craisinlord.antarchy.forge.network.AntarchyForgeNetworkCore;
import com.craisinlord.antarchy.forge.network.AntarchyPayloadContext;
import com.craisinlord.antarchy.forge.network.AntarchyForgePayloadRegistrar;

public final class AntarchyGravityNetworking {
    private AntarchyGravityNetworking() {
    }

    public static void register(AntarchyForgePayloadRegistrar registrar) {
        com.craisinlord.antarchy.content.portalgun.PortalGunIndicatorSync.setSender(AntarchyForgeNetworkCore::sendToPlayer);
        registrar.playToClient(
                com.craisinlord.antarchy.content.network.PortalGunIndicatorPayload.TYPE,
                com.craisinlord.antarchy.content.network.PortalGunIndicatorPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> com.craisinlord.antarchy.content.client.PortalGunIndicatorClientState.update(payload))
        ).playToServer(
                com.craisinlord.antarchy.content.network.PortalGunIndicatorRequestPayload.TYPE,
                com.craisinlord.antarchy.content.network.PortalGunIndicatorRequestPayload.STREAM_CODEC,
                AntarchyGravityNetworking::handlePortalGunIndicatorRequest
        ).playToServer(
                GravityGunPrimaryPayload.TYPE,
                GravityGunPrimaryPayload.STREAM_CODEC,
                AntarchyGravityNetworking::handleGravityGunPrimary
        ).playToServer(
                PortalGunPrimaryPayload.TYPE,
                PortalGunPrimaryPayload.STREAM_CODEC,
                AntarchyGravityNetworking::handlePortalGunPrimary
        ).playToServer(
                com.craisinlord.antarchy.content.network.PortalGunMoonAimPayload.TYPE,
                com.craisinlord.antarchy.content.network.PortalGunMoonAimPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer serverPlayer) {
                        com.craisinlord.antarchy.content.portalgun.PortalGunMoonAimSync.handle(serverPlayer, payload);
                    }
                })
        ).playToServer(
                com.craisinlord.antarchy.content.network.PortalGunResetPayload.TYPE,
                com.craisinlord.antarchy.content.network.PortalGunResetPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer serverPlayer) {
                        com.craisinlord.antarchy.content.portalgun.PortalGunResetManager.handleInput(serverPlayer, payload.action());
                    }
                })
        ).playToServer(
                EyeOfStormPrimaryPayload.TYPE,
                EyeOfStormPrimaryPayload.STREAM_CODEC,
                AntarchyGravityNetworking::handleEyeOfStormPrimary
        ).playToServer(
                GravityGunScrollPayload.TYPE,
                GravityGunScrollPayload.STREAM_CODEC,
                AntarchyGravityNetworking::handleGravityGunScroll
        ).playToServer(
                TemporalTunerScrollPayload.TYPE,
                TemporalTunerScrollPayload.STREAM_CODEC,
                AntarchyGravityNetworking::handleTemporalTunerScroll
        ).playToServer(
                BigBerthaModeCyclePayload.TYPE,
                BigBerthaModeCyclePayload.STREAM_CODEC,
                AntarchyGravityNetworking::handleBigBerthaModeCycle
        ).playToServer(
                RoyalGuardianSwordModeCyclePayload.TYPE,
                RoyalGuardianSwordModeCyclePayload.STREAM_CODEC,
                AntarchyGravityNetworking::handleRoyalGuardianSwordModeCycle
        ).playToServer(
                com.craisinlord.antarchy.content.network.DiamondMinecartInputPayload.TYPE,
                com.craisinlord.antarchy.content.network.DiamondMinecartInputPayload.STREAM_CODEC,
                AntarchyGravityNetworking::handleDiamondMinecartInput
        );
    }

    private static void handleGravityGunScroll(GravityGunScrollPayload payload, AntarchyPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }

            if (!(serverPlayer.getMainHandItem().getItem() instanceof GravityGunItem)) {
                return;
            }

            GravityGunItem.adjustHeldDistance(serverPlayer.getMainHandItem(), payload.distanceDelta());
        });
    }

    private static void handleDiamondMinecartInput(com.craisinlord.antarchy.content.network.DiamondMinecartInputPayload payload, AntarchyPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) return;
            if (!(serverPlayer.getVehicle() instanceof DiamondMinecartEntity cart)) return;
            cart.onInputReceived(payload.inputFlags());
        });
    }

    private static void handleBigBerthaModeCycle(BigBerthaModeCyclePayload payload, AntarchyPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }

            if (!(serverPlayer.getMainHandItem().getItem() instanceof BigBerthaItem bigBerthaItem)) {
                return;
            }

            bigBerthaItem.tryCycleModeFromInput(serverPlayer.serverLevel(), serverPlayer, serverPlayer.getMainHandItem());
        });
    }

    private static void handleTemporalTunerScroll(TemporalTunerScrollPayload payload, AntarchyPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer
                    && TemporalTunerItem.isAvailable(serverPlayer)) {
                TemporalTunerItem.adjust(serverPlayer, payload.delta());
            }
        });
    }

    private static void handleRoyalGuardianSwordModeCycle(RoyalGuardianSwordModeCyclePayload payload, AntarchyPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)
                    || !(serverPlayer.getMainHandItem().getItem() instanceof com.craisinlord.antarchy.content.item.RoyalGuardianSwordItem sword)) {
                return;
            }
            sword.tryCycleModeFromInput(serverPlayer.serverLevel(), serverPlayer, serverPlayer.getMainHandItem());
        });
    }

    private static void handleGravityGunPrimary(GravityGunPrimaryPayload payload, AntarchyPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }

            if (!(serverPlayer.getMainHandItem().getItem() instanceof GravityGunItem gravityGunItem)) {
                return;
            }

            gravityGunItem.firePrimary(serverPlayer.serverLevel(), serverPlayer, serverPlayer.getMainHandItem());
        });
    }

    private static void handlePortalGunPrimary(PortalGunPrimaryPayload payload, AntarchyPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }

            ItemStack stack = payload.offhand() ? serverPlayer.getOffhandItem() : serverPlayer.getMainHandItem();
            if (!(stack.getItem() instanceof PortalGunItem portalGunItem)) {
                return;
            }

            portalGunItem.firePrimary(serverPlayer.serverLevel(), serverPlayer, stack, payload.offhand());
        });
    }


    private static void handlePortalGunIndicatorRequest(com.craisinlord.antarchy.content.network.PortalGunIndicatorRequestPayload payload, AntarchyPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                com.craisinlord.antarchy.content.portalgun.PortalGunIndicatorSync.handleRequest(serverPlayer, payload);
            }
        });
    }

    private static void handleEyeOfStormPrimary(EyeOfStormPrimaryPayload payload, AntarchyPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }

            if (!(serverPlayer.getMainHandItem().getItem() instanceof EyeOfTheStormItem eyeOfTheStormItem)) {
                return;
            }

            eyeOfTheStormItem.firePrimary(serverPlayer.serverLevel(), serverPlayer, serverPlayer.getMainHandItem());
        });
    }
}
