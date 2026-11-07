package com.craisinlord.antarchy.neoforge.network;

import com.craisinlord.antarchy.content.client.PortalGunIndicatorClientState;
import com.craisinlord.antarchy.content.entity.DiamondMinecartEntity;
import com.craisinlord.antarchy.content.item.BigBerthaItem;
import com.craisinlord.antarchy.content.item.EyeOfTheStormItem;
import com.craisinlord.antarchy.content.item.GravityGunItem;
import com.craisinlord.antarchy.content.item.PortalGunItem;
import com.craisinlord.antarchy.content.item.RoyalGuardianSwordItem;
import com.craisinlord.antarchy.content.item.TemporalTunerItem;
import com.craisinlord.antarchy.content.network.BigBerthaModeCyclePayload;
import com.craisinlord.antarchy.content.network.DiamondMinecartInputPayload;
import com.craisinlord.antarchy.content.network.EyeOfStormPrimaryPayload;
import com.craisinlord.antarchy.content.network.GravityGunPrimaryPayload;
import com.craisinlord.antarchy.content.network.GravityGunScrollPayload;
import com.craisinlord.antarchy.content.network.PortalGunIndicatorPayload;
import com.craisinlord.antarchy.content.network.PortalGunIndicatorRequestPayload;
import com.craisinlord.antarchy.content.network.PortalGunMoonAimPayload;
import com.craisinlord.antarchy.content.network.PortalGunPrimaryPayload;
import com.craisinlord.antarchy.content.network.PortalGunResetPayload;
import com.craisinlord.antarchy.content.network.RoyalGuardianSwordModeCyclePayload;
import com.craisinlord.antarchy.content.network.TemporalTunerScrollPayload;
import com.craisinlord.antarchy.content.portalgun.PortalGunIndicatorSync;
import com.craisinlord.antarchy.content.portalgun.PortalGunMoonAimSync;
import com.craisinlord.antarchy.content.portalgun.PortalGunResetManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class AntarchyGravityNetworking {
    private AntarchyGravityNetworking() {
    }

    public static void register(PayloadRegistrar registrar) {
        registrar.playToClient(
                PortalGunIndicatorPayload.TYPE,
                PortalGunIndicatorPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> PortalGunIndicatorClientState.update(payload))
        ).playToServer(
                PortalGunIndicatorRequestPayload.TYPE,
                PortalGunIndicatorRequestPayload.STREAM_CODEC,
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
                PortalGunMoonAimPayload.TYPE,
                PortalGunMoonAimPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer serverPlayer) {
                        PortalGunMoonAimSync.handle(serverPlayer, payload);
                    }
                })
        ).playToServer(
                PortalGunResetPayload.TYPE,
                PortalGunResetPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer serverPlayer) {
                        PortalGunResetManager.handleInput(serverPlayer, payload.action());
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
                DiamondMinecartInputPayload.TYPE,
                DiamondMinecartInputPayload.STREAM_CODEC,
                AntarchyGravityNetworking::handleDiamondMinecartInput
        );
    }

    private static void handleGravityGunScroll(GravityGunScrollPayload payload, IPayloadContext context) {
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

    private static void handleDiamondMinecartInput(DiamondMinecartInputPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }
            if (!(serverPlayer.getVehicle() instanceof DiamondMinecartEntity cart)) {
                return;
            }
            cart.onInputReceived(payload.inputFlags());
        });
    }

    private static void handleBigBerthaModeCycle(BigBerthaModeCyclePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }

            if (!(serverPlayer.getMainHandItem().getItem() instanceof BigBerthaItem bigBerthaItem)) {
                return;
            }

            bigBerthaItem.tryCycleModeFromInput(serverPlayer.level(), serverPlayer, serverPlayer.getMainHandItem());
        });
    }

    private static void handleTemporalTunerScroll(TemporalTunerScrollPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer
                    && TemporalTunerItem.isAvailable(serverPlayer)) {
                TemporalTunerItem.adjust(serverPlayer, payload.delta());
            }
        });
    }

    private static void handleRoyalGuardianSwordModeCycle(RoyalGuardianSwordModeCyclePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)
                    || !(serverPlayer.getMainHandItem().getItem() instanceof RoyalGuardianSwordItem sword)) {
                return;
            }
            sword.tryCycleModeFromInput(serverPlayer.level(), serverPlayer, serverPlayer.getMainHandItem());
        });
    }

    private static void handleGravityGunPrimary(GravityGunPrimaryPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }

            if (!(serverPlayer.getMainHandItem().getItem() instanceof GravityGunItem gravityGunItem)) {
                return;
            }

            gravityGunItem.firePrimary(serverPlayer.level(), serverPlayer, serverPlayer.getMainHandItem());
        });
    }

    private static void handlePortalGunPrimary(PortalGunPrimaryPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }

            ItemStack stack = payload.offhand() ? serverPlayer.getOffhandItem() : serverPlayer.getMainHandItem();
            if (!(stack.getItem() instanceof PortalGunItem portalGunItem)) {
                return;
            }

            portalGunItem.firePrimary(serverPlayer.level(), serverPlayer, stack, payload.offhand());
        });
    }

    private static void handlePortalGunIndicatorRequest(PortalGunIndicatorRequestPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                PortalGunIndicatorSync.handleRequest(serverPlayer, payload);
            }
        });
    }

    private static void handleEyeOfStormPrimary(EyeOfStormPrimaryPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }

            if (!(serverPlayer.getMainHandItem().getItem() instanceof EyeOfTheStormItem eyeOfTheStormItem)) {
                return;
            }

            eyeOfTheStormItem.firePrimary(serverPlayer.level(), serverPlayer, serverPlayer.getMainHandItem());
        });
    }
}
