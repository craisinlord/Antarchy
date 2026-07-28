package com.craisinlord.antarchy.forge.network;

import com.craisinlord.antarchy.content.entity.DiamondMinecartEntity;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.item.BigBerthaItem;
import com.craisinlord.antarchy.content.item.GravityGunItem;
import com.craisinlord.antarchy.content.network.BigBerthaModeCyclePayload;
import com.craisinlord.antarchy.content.network.DiamondMinecartInputPayload;
import com.craisinlord.antarchy.content.network.GravityGunPrimaryPayload;
import com.craisinlord.antarchy.content.network.GravityGunScrollPayload;
import com.craisinlord.antarchy.content.network.GravityStatePayload;
import com.craisinlord.antarchy.content.network.ImpactShakePayload;
import com.craisinlord.antarchy.content.client.CameraShakeClientState;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public final class AntarchyGravityNetworking {
    private AntarchyGravityNetworking() {
    }

    public static void register() {
        AntarchyForgeNetworkCore.registerS2C(GravityStatePayload.class, GravityStatePayload.STREAM_CODEC, AntarchyGravityNetworking::handleGravityState);
        AntarchyForgeNetworkCore.registerS2C(ImpactShakePayload.class, ImpactShakePayload.STREAM_CODEC, AntarchyGravityNetworking::handleImpactShake);
        AntarchyForgeNetworkCore.registerC2S(GravityGunPrimaryPayload.class, GravityGunPrimaryPayload.STREAM_CODEC, AntarchyGravityNetworking::handleGravityGunPrimary);
        AntarchyForgeNetworkCore.registerC2S(GravityGunScrollPayload.class, GravityGunScrollPayload.STREAM_CODEC, AntarchyGravityNetworking::handleGravityGunScroll);
        AntarchyForgeNetworkCore.registerC2S(BigBerthaModeCyclePayload.class, BigBerthaModeCyclePayload.STREAM_CODEC, AntarchyGravityNetworking::handleBigBerthaModeCycle);
        AntarchyForgeNetworkCore.registerC2S(DiamondMinecartInputPayload.class, DiamondMinecartInputPayload.STREAM_CODEC, AntarchyGravityNetworking::handleDiamondMinecartInput);
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
        AntarchyForgeNetworkCore.sendToPlayer(target, payload);
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
        AntarchyForgeNetworkCore.sendToTrackingEntity(entity, payload);
    }

    private static void handleGravityState(GravityStatePayload payload) {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        Entity entity = player.level().getEntity(payload.entityId());
        if (entity == null && player.getId() == payload.entityId()) {
            entity = player;
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
    }

    private static void handleImpactShake(ImpactShakePayload payload) {
        CameraShakeClientState.triggerImpact(
                new Vec3(payload.x(), payload.y(), payload.z()),
                payload.intensity(),
                payload.durationTicks(),
                payload.radius()
        );
    }

    private static void handleGravityGunScroll(ServerPlayer serverPlayer, GravityGunScrollPayload payload) {
        if (!(serverPlayer.getMainHandItem().getItem() instanceof GravityGunItem)) {
            return;
        }

        GravityGunItem.adjustHeldDistance(serverPlayer.getMainHandItem(), payload.distanceDelta());
    }

    private static void handleDiamondMinecartInput(ServerPlayer serverPlayer, DiamondMinecartInputPayload payload) {
        if (!(serverPlayer.getVehicle() instanceof DiamondMinecartEntity cart)) return;
        cart.onInputReceived(payload.inputFlags());
    }

    private static void handleBigBerthaModeCycle(ServerPlayer serverPlayer, BigBerthaModeCyclePayload payload) {
        if (!(serverPlayer.getMainHandItem().getItem() instanceof BigBerthaItem bigBerthaItem)) {
            return;
        }

        bigBerthaItem.tryCycleModeWhileCoolingDown(serverPlayer.serverLevel(), serverPlayer, serverPlayer.getMainHandItem());
    }

    private static void handleGravityGunPrimary(ServerPlayer serverPlayer, GravityGunPrimaryPayload payload) {
        if (!(serverPlayer.getMainHandItem().getItem() instanceof GravityGunItem gravityGunItem)) {
            return;
        }

        gravityGunItem.firePrimary(serverPlayer.serverLevel(), serverPlayer, serverPlayer.getMainHandItem());
    }
}
