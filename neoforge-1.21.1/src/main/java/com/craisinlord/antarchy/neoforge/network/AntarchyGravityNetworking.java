package com.craisinlord.antarchy.neoforge.network;

import com.craisinlord.antarchy.content.entity.DiamondMinecartEntity;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.item.BigBerthaItem;
import com.craisinlord.antarchy.content.item.EyeOfTheStormItem;
import com.craisinlord.antarchy.content.item.GravityGunItem;
import com.craisinlord.antarchy.content.item.PortalGunItem;
import com.craisinlord.antarchy.content.network.BigBerthaModeCyclePayload;
import com.craisinlord.antarchy.content.network.EyeOfStormPrimaryPayload;
import com.craisinlord.antarchy.content.network.GravityGunPrimaryPayload;
import com.craisinlord.antarchy.content.network.GravityGunScrollPayload;
import com.craisinlord.antarchy.content.network.GravityStatePayload;
import com.craisinlord.antarchy.content.network.ImpactShakePayload;
import com.craisinlord.antarchy.content.network.PortalGunPrimaryPayload;
import com.craisinlord.antarchy.content.client.CameraShakeClientState;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class AntarchyGravityNetworking {
    private AntarchyGravityNetworking() {
    }

    public static void register(PayloadRegistrar registrar) {
        registrar.playToClient(
                GravityStatePayload.TYPE,
                GravityStatePayload.STREAM_CODEC,
                AntarchyGravityNetworking::handleGravityState
        ).playToClient(
                ImpactShakePayload.TYPE,
                ImpactShakePayload.STREAM_CODEC,
                AntarchyGravityNetworking::handleImpactShake
        ).playToServer(
                GravityGunPrimaryPayload.TYPE,
                GravityGunPrimaryPayload.STREAM_CODEC,
                AntarchyGravityNetworking::handleGravityGunPrimary
        ).playToServer(
                PortalGunPrimaryPayload.TYPE,
                PortalGunPrimaryPayload.STREAM_CODEC,
                AntarchyGravityNetworking::handlePortalGunPrimary
        ).playToServer(
                EyeOfStormPrimaryPayload.TYPE,
                EyeOfStormPrimaryPayload.STREAM_CODEC,
                AntarchyGravityNetworking::handleEyeOfStormPrimary
        ).playToServer(
                GravityGunScrollPayload.TYPE,
                GravityGunScrollPayload.STREAM_CODEC,
                AntarchyGravityNetworking::handleGravityGunScroll
        ).playToServer(
                BigBerthaModeCyclePayload.TYPE,
                BigBerthaModeCyclePayload.STREAM_CODEC,
                AntarchyGravityNetworking::handleBigBerthaModeCycle
        ).playToServer(
                com.craisinlord.antarchy.content.network.DiamondMinecartInputPayload.TYPE,
                com.craisinlord.antarchy.content.network.DiamondMinecartInputPayload.STREAM_CODEC,
                AntarchyGravityNetworking::handleDiamondMinecartInput
        );
    }

    public static void syncToPlayer(ServerPlayer target, Entity entity) {
        GravityStatePayload payload = new GravityStatePayload(
                entity.getId(),
                AntarchyGravityApi.getGravityDirection(entity),
                AntarchyGravityApi.getPrevGravityDirection(entity),
                AntarchyGravityApi.isGravityForced(entity),
                AntarchyGravityApi.getTransitionDuration(entity),
                AntarchyGravityApi.getTransitionRemaining(entity)
        );
        PacketDistributor.sendToPlayer(target, payload);
    }

    public static void syncEntity(Entity entity) {
        GravityStatePayload payload = new GravityStatePayload(
                entity.getId(),
                AntarchyGravityApi.getGravityDirection(entity),
                AntarchyGravityApi.getPrevGravityDirection(entity),
                AntarchyGravityApi.isGravityForced(entity),
                AntarchyGravityApi.getTransitionDuration(entity),
                AntarchyGravityApi.getTransitionRemaining(entity)
        );
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, payload);
    }

    private static void handleGravityState(GravityStatePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Entity entity = context.player().level().getEntity(payload.entityId());
            // On relog the local player entity may not yet be in the level's entity lookup,
            // so fall back to context.player() to ensure the local player always gets synced.
            if (entity == null && context.player().getId() == payload.entityId()) {
                entity = context.player();
            }
            if (entity == null) {
                return;
            }

            AntarchyGravityApi.applySyncedState(
                    entity,
                    payload.direction(),
                    payload.previousDirection(),
                    payload.forced(),
                    payload.transitionDuration(),
                    payload.transitionRemaining()
            );
        });
    }

    private static void handleImpactShake(ImpactShakePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> CameraShakeClientState.triggerImpact(
                new Vec3(payload.x(), payload.y(), payload.z()),
                payload.intensity(),
                payload.durationTicks(),
                payload.radius()
        ));
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

    private static void handleDiamondMinecartInput(com.craisinlord.antarchy.content.network.DiamondMinecartInputPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) return;
            if (!(serverPlayer.getVehicle() instanceof DiamondMinecartEntity cart)) return;
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

            bigBerthaItem.tryCycleModeWhileCoolingDown(serverPlayer.serverLevel(), serverPlayer, serverPlayer.getMainHandItem());
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

            gravityGunItem.firePrimary(serverPlayer.serverLevel(), serverPlayer, serverPlayer.getMainHandItem());
        });
    }

    private static void handlePortalGunPrimary(PortalGunPrimaryPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }

            if (!(serverPlayer.getMainHandItem().getItem() instanceof PortalGunItem portalGunItem)) {
                return;
            }

            portalGunItem.firePrimary(serverPlayer.serverLevel(), serverPlayer, serverPlayer.getMainHandItem());
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

            eyeOfTheStormItem.firePrimary(serverPlayer.serverLevel(), serverPlayer, serverPlayer.getMainHandItem());
        });
    }
}
