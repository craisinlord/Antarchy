package com.craisinlord.antarchy.neoforge.network;

import com.craisinlord.antarchy.content.entity.DiamondMinecartEntity;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
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
import com.craisinlord.antarchy.content.network.GravityStatePayload;
import com.craisinlord.antarchy.content.network.ImpactShakePayload;
import com.craisinlord.antarchy.content.network.PortalGunPrimaryPayload;
import com.craisinlord.antarchy.content.client.CameraShakeClientState;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class AntarchyGravityNetworking {
    private AntarchyGravityNetworking() {
    }

    public static void register(PayloadRegistrar registrar) {
        com.craisinlord.antarchy.content.portalgun.PortalGunIndicatorSync.setSender(PacketDistributor::sendToPlayer);
        registrar.playToClient(
                com.craisinlord.antarchy.content.network.PortalGunIndicatorPayload.TYPE,
                com.craisinlord.antarchy.content.network.PortalGunIndicatorPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> com.craisinlord.antarchy.content.client.PortalGunIndicatorClientState.update(payload))
        ).playToServer(
                com.craisinlord.antarchy.content.network.PortalGunIndicatorRequestPayload.TYPE,
                com.craisinlord.antarchy.content.network.PortalGunIndicatorRequestPayload.STREAM_CODEC,
                AntarchyGravityNetworking::handlePortalGunIndicatorRequest
        ).playToClient(
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
                com.craisinlord.antarchy.content.network.PortalGunTransitPayload.TYPE,
                com.craisinlord.antarchy.content.network.PortalGunTransitPayload.STREAM_CODEC,
                (payload, context) -> {
                    if (context.player() instanceof ServerPlayer serverPlayer && serverPlayer.getServer() != null && serverPlayer.getServer().isSameThread()) {
                        com.craisinlord.antarchy.content.portalgun.PortalGunTransitHandler.handle(serverPlayer, payload);
                        return;
                    }
                    context.enqueueWork(() -> {
                        if (context.player() instanceof ServerPlayer serverPlayer) {
                            com.craisinlord.antarchy.content.portalgun.PortalGunTransitHandler.handle(serverPlayer, payload);
                        }
                    });
                }
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

            bigBerthaItem.tryCycleModeFromInput(serverPlayer.serverLevel(), serverPlayer, serverPlayer.getMainHandItem());
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
                    || !(serverPlayer.getMainHandItem().getItem() instanceof com.craisinlord.antarchy.content.item.RoyalGuardianSwordItem sword)) {
                return;
            }
            sword.tryCycleModeFromInput(serverPlayer.serverLevel(), serverPlayer, serverPlayer.getMainHandItem());
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

            ItemStack stack = payload.offhand() ? serverPlayer.getOffhandItem() : serverPlayer.getMainHandItem();
            if (!(stack.getItem() instanceof PortalGunItem portalGunItem)) {
                return;
            }

            portalGunItem.firePrimary(serverPlayer.serverLevel(), serverPlayer, stack, payload.offhand());
        });
    }


    private static void handlePortalGunIndicatorRequest(com.craisinlord.antarchy.content.network.PortalGunIndicatorRequestPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                com.craisinlord.antarchy.content.portalgun.PortalGunIndicatorSync.handleRequest(serverPlayer, payload);
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

            eyeOfTheStormItem.firePrimary(serverPlayer.serverLevel(), serverPlayer, serverPlayer.getMainHandItem());
        });
    }
}
