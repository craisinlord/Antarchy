package com.craisinlord.antarchy.forge.registry;

import com.craisinlord.antarchy.content.network.BloodCrystalKatanaTrailPayload;
import com.craisinlord.antarchy.content.network.ToggleTigerEyeCamouflagePayload;
import com.craisinlord.antarchy.content.item.TigerEyeArmorUtil;
import com.craisinlord.antarchy.content.tigereye.TigerEyeCamouflageController;
import com.craisinlord.antarchy.forge.network.AntarchyGravityNetworking;
import com.craisinlord.antarchy.forge.network.BrutalflyElytraNetworking;
import com.craisinlord.antarchy.forge.network.DorrieJumpNetworking;
import com.craisinlord.antarchy.forge.network.HerculesBeetleNetworking;
import com.craisinlord.antarchy.forge.network.SpringyBootsNetworking;
import com.craisinlord.antarchy.forge.network.AntarchyForgePayloadRegistrar;

public class AntarchyForgePayloadHandlers {
    private AntarchyForgePayloadHandlers() {}

    public static void register() {
        registerPayloadHandlers(new AntarchyForgePayloadRegistrar());
    }

    static void registerPayloadHandlers(AntarchyForgePayloadRegistrar registrar) {
        registrar.playToClient(
                com.craisinlord.antarchy.content.network.ElythiaEventPayload.TYPE,
                com.craisinlord.antarchy.content.network.ElythiaEventPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> com.craisinlord.antarchy.content.client.ElythiaEventClient.handle(payload))
        );
        registrar.playToServer(
                com.craisinlord.antarchy.content.network.AntarchyGamePayload.TYPE,
                com.craisinlord.antarchy.content.network.AntarchyGamePayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> {
                    if (ctx.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        com.craisinlord.antarchy.content.network.AntarchyGameNetworkHandler.handle(player, payload);
                    }
                })
        );
        registrar.playToClient(
                com.craisinlord.antarchy.content.network.AntarchyGameResultPayload.TYPE,
                com.craisinlord.antarchy.content.network.AntarchyGameResultPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> com.craisinlord.antarchy.content.client.AntarchyGameClientState.update(payload))
        );
        AntarchyGravityNetworking.register(registrar);
        BrutalflyElytraNetworking.register(registrar);
        SpringyBootsNetworking.register(registrar);
        DorrieJumpNetworking.register(registrar);
        HerculesBeetleNetworking.register(registrar);
        registrar.playToClient(
                com.craisinlord.antarchy.content.network.KingJudgmentFlashPayload.TYPE,
                com.craisinlord.antarchy.content.network.KingJudgmentFlashPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() ->
                        com.craisinlord.antarchy.content.client.KingJudgmentFlashClientState.trigger(payload.durationTicks()))
        );
        registrar.playToClient(
                com.craisinlord.antarchy.content.network.BloodglassStatePayload.TYPE,
                com.craisinlord.antarchy.content.network.BloodglassStatePayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() ->
                        com.craisinlord.antarchy.content.client.BloodglassClientState.update(payload.shieldsActive(), payload.shieldsMax())
                )
        );
        registrar.playToClient(
                com.craisinlord.antarchy.content.network.TigerEyeCamouflageStatePayload.TYPE,
                com.craisinlord.antarchy.content.network.TigerEyeCamouflageStatePayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() ->
                        com.craisinlord.antarchy.content.client.TigerEyeCamouflageClientState.update(payload.entityId(), payload.active(), payload.blockStateId())
                )
        );
        registrar.playToServer(
                com.craisinlord.antarchy.content.network.ActivateRoyalBoundaryPayload.TYPE,
                com.craisinlord.antarchy.content.network.ActivateRoyalBoundaryPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> {
                    if (ctx.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        com.craisinlord.antarchy.content.effect.RoyalBoundaryManager.activate(player);
                    }
                })
        );
        registrar.playToServer(
                com.craisinlord.antarchy.content.network.ToggleRoyalInversionPayload.TYPE,
                com.craisinlord.antarchy.content.network.ToggleRoyalInversionPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> {
                    if (ctx.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        com.craisinlord.antarchy.content.item.RoyalAssailantArmorItem.toggleInversion(player);
                    }
                })
        );
        registrar.playToServer(
                com.craisinlord.antarchy.content.network.OpenGiantFryingPanPayload.TYPE,
                com.craisinlord.antarchy.content.network.OpenGiantFryingPanPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> {
                    if (ctx.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        com.craisinlord.antarchy.content.item.GiantFryingPanItem.openHeldMenu(player);
                    }
                })
        );
        registrar.playToServer(
                com.craisinlord.antarchy.content.network.RoyalMountActionPayload.TYPE,
                com.craisinlord.antarchy.content.network.RoyalMountActionPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> {
                    if (ctx.player() instanceof net.minecraft.server.level.ServerPlayer mountingPlayer
                            && payload.action() == com.craisinlord.antarchy.content.network.RoyalMountActionPayload.MOUNT) {
                        com.craisinlord.antarchy.content.entity.royal.RoyalMountEntity.tryAssistedMount(mountingPlayer);
                        return;
                    }
                    if (!(ctx.player() instanceof net.minecraft.server.level.ServerPlayer player)
                            || !(player.getVehicle() instanceof com.craisinlord.antarchy.content.entity.royal.RoyalMountEntity mount)) {
                        return;
                    }
                    switch (payload.action()) {
                        case com.craisinlord.antarchy.content.network.RoyalMountActionPayload.FLIGHT_TOGGLE -> mount.toggleMountedFlight(player);
                        case com.craisinlord.antarchy.content.network.RoyalMountActionPayload.BITE -> mount.handleMountedBite(player);
                        case com.craisinlord.antarchy.content.network.RoyalMountActionPayload.SPIT -> mount.handleMountedSpit(player);
                        case com.craisinlord.antarchy.content.network.RoyalMountActionPayload.BEAM -> mount.handleMountedBeam(player);
                        case com.craisinlord.antarchy.content.network.RoyalMountActionPayload.SIGNATURE -> mount.handleMountedSignature(player);
                        default -> {
                        }
                    }
                })
        );
        registrar.playToServer(
                com.craisinlord.antarchy.content.network.RoyalMountVerticalPayload.TYPE,
                com.craisinlord.antarchy.content.network.RoyalMountVerticalPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> {
                    if (ctx.player() instanceof net.minecraft.server.level.ServerPlayer player
                            && player.getVehicle() instanceof com.craisinlord.antarchy.content.entity.royal.RoyalMountEntity mount) {
                        mount.setRiderAscend(payload.ascend());
                        mount.setRiderDescend(payload.descend());
                    }
                })
        );
        registrar.playToServer(
                ToggleTigerEyeCamouflagePayload.TYPE,
                ToggleTigerEyeCamouflagePayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> {
                    if (!(ctx.player() instanceof net.minecraft.server.level.ServerPlayer player)) {
                        return;
                    }
                    TigerEyeCamouflageController.ToggleResult result = TigerEyeCamouflageController.toggle(player);
                    if (result != TigerEyeCamouflageController.ToggleResult.NO_CHANGE) {
                        com.craisinlord.antarchy.forge.network.AntarchyForgeNetworkCore.sendToTrackingEntity(
                                player,
                                com.craisinlord.antarchy.content.tigereye.TigerEyeCamouflageSync.payload(player)
                        );
                    }
                    String messageKey = switch (result) {
                        case ACTIVATED -> "message.antarchy.tiger_eye_camouflage.activated";
                        case DEACTIVATED -> "message.antarchy.tiger_eye_camouflage.disabled";
                        case FULL_SET_REQUIRED -> TigerEyeArmorUtil.countEquippedPieces(player) > 0
                                ? "message.antarchy.tiger_eye_camouflage.full_set_required"
                                : null;
                        case INVALID_BLOCK -> "message.antarchy.tiger_eye_camouflage.invalid_block";
                        case NO_CHANGE -> null;
                    };
                    if (messageKey != null) {
                        player.displayClientMessage(net.minecraft.network.chat.Component.translatable(messageKey), true);
                    }
                })
        );
        registrar.playToClient(
                BloodCrystalKatanaTrailPayload.TYPE,
                BloodCrystalKatanaTrailPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() ->
                        com.craisinlord.antarchy.content.client.BloodCrystalKatanaTrailClientState.trigger(payload.entityId(), payload.durationTicks())
                )
        );
        registrar.playToClient(
                com.craisinlord.antarchy.content.network.ScorpionWhipTetherPayload.TYPE,
                com.craisinlord.antarchy.content.network.ScorpionWhipTetherPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() ->
                        com.craisinlord.antarchy.content.client.ScorpionWhipTetherClientState.update(payload.playerId(), payload.targetId())
                )
        );
        registrar.playToClient(
                com.craisinlord.antarchy.content.network.WormHookTetherPayload.TYPE,
                com.craisinlord.antarchy.content.network.WormHookTetherPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() ->
                        com.craisinlord.antarchy.content.client.WormHookTetherClientState.update(payload.playerId(), payload.hookId())
                )
        );
        registrar.playToClient(
                com.craisinlord.antarchy.content.network.HordeIntensityPayload.TYPE,
                com.craisinlord.antarchy.content.network.HordeIntensityPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() ->
                        com.craisinlord.antarchy.content.client.HordeClientState.update(payload.intensity())
                )
        );
    }
}
