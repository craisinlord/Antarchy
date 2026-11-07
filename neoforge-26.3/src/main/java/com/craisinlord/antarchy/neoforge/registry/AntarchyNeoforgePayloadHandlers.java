package com.craisinlord.antarchy.neoforge.registry;

import com.craisinlord.antarchy.content.client.AntTransitClientState;
import com.craisinlord.antarchy.content.client.AntarchyGameClientState;
import com.craisinlord.antarchy.content.client.BloodCrystalKatanaTrailClientState;
import com.craisinlord.antarchy.content.client.BloodglassClientState;
import com.craisinlord.antarchy.content.client.ElythiaEchoes;
import com.craisinlord.antarchy.content.client.ElythiaEventClient;
import com.craisinlord.antarchy.content.client.HordeClientState;
import com.craisinlord.antarchy.content.client.KingJudgmentFlashClientState;
import com.craisinlord.antarchy.content.client.ScorpionWhipTetherClientState;
import com.craisinlord.antarchy.content.client.ThoraxisEventClient;
import com.craisinlord.antarchy.content.client.ThrowbackClientState;
import com.craisinlord.antarchy.content.client.TigerEyeCamouflageClientState;
import com.craisinlord.antarchy.content.client.WormHookTetherClientState;
import com.craisinlord.antarchy.content.effect.RoyalBoundaryManager;
import com.craisinlord.antarchy.content.entity.royal.RoyalMountEntity;
import com.craisinlord.antarchy.content.item.GiantFryingPanItem;
import com.craisinlord.antarchy.content.item.RoyalAssailantArmorItem;
import com.craisinlord.antarchy.content.item.TigerEyeArmorUtil;
import com.craisinlord.antarchy.content.network.ActivateRoyalBoundaryPayload;
import com.craisinlord.antarchy.content.network.AntTransitPayload;
import com.craisinlord.antarchy.content.network.AntarchyGameNetworkHandler;
import com.craisinlord.antarchy.content.network.AntarchyGamePayload;
import com.craisinlord.antarchy.content.network.AntarchyGameResultPayload;
import com.craisinlord.antarchy.content.network.BloodCrystalKatanaTrailPayload;
import com.craisinlord.antarchy.content.network.BloodglassStatePayload;
import com.craisinlord.antarchy.content.network.ElythiaEchoTestPayload;
import com.craisinlord.antarchy.content.network.ElythiaEventPayload;
import com.craisinlord.antarchy.content.network.HordeIntensityPayload;
import com.craisinlord.antarchy.content.network.KingJudgmentFlashPayload;
import com.craisinlord.antarchy.content.network.OpenGiantFryingPanPayload;
import com.craisinlord.antarchy.content.network.RoyalMountActionPayload;
import com.craisinlord.antarchy.content.network.RoyalMountVerticalPayload;
import com.craisinlord.antarchy.content.network.ScorpionWhipTetherPayload;
import com.craisinlord.antarchy.content.network.ThoraxisBubbleUpdatePayload;
import com.craisinlord.antarchy.content.network.ThoraxisEventPayload;
import com.craisinlord.antarchy.content.network.ThrowbackZonePayload;
import com.craisinlord.antarchy.content.network.TigerEyeCamouflageStatePayload;
import com.craisinlord.antarchy.content.network.ToggleRoyalInversionPayload;
import com.craisinlord.antarchy.content.network.ToggleTigerEyeCamouflagePayload;
import com.craisinlord.antarchy.content.network.WormHookTetherPayload;
import com.craisinlord.antarchy.content.tigereye.TigerEyeCamouflageController;
import com.craisinlord.antarchy.content.tigereye.TigerEyeCamouflageSync;
import com.craisinlord.antarchy.neoforge.network.AntarchyGravityNetworking;
import com.craisinlord.antarchy.neoforge.network.BrutalflyElytraNetworking;
import com.craisinlord.antarchy.neoforge.network.DorrieJumpNetworking;
import com.craisinlord.antarchy.neoforge.network.HerculesBeetleNetworking;
import com.craisinlord.antarchy.neoforge.network.SpringyBootsNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public class AntarchyNeoforgePayloadHandlers {
    private AntarchyNeoforgePayloadHandlers() {}

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(AntarchyNeoforgePayloadHandlers::registerPayloadHandlers);
    }

    static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");
        registrar.playToClient(
                AntTransitPayload.TYPE,
                AntTransitPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> AntTransitClientState.handle(payload))
        );
        registrar.playToClient(
                ElythiaEchoTestPayload.TYPE,
                ElythiaEchoTestPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() ->
                        ElythiaEchoes.handleTest(payload.immediate())
                )
        );
        registrar.playToClient(
                ElythiaEventPayload.TYPE,
                ElythiaEventPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> ElythiaEventClient.handle(payload))
        );
        registrar.playToClient(
                ThrowbackZonePayload.TYPE,
                ThrowbackZonePayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> ThrowbackClientState.update(payload.strength()))
        );
        registrar.playToClient(
                ThoraxisEventPayload.TYPE,
                ThoraxisEventPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> ThoraxisEventClient.handle(payload))
        );
        registrar.playToClient(
                ThoraxisBubbleUpdatePayload.TYPE,
                ThoraxisBubbleUpdatePayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> ThoraxisEventClient.handleUpdate(payload))
        );
        registrar.playToServer(
                AntarchyGamePayload.TYPE,
                AntarchyGamePayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> {
                    if (ctx.player() instanceof ServerPlayer player) {
                        AntarchyGameNetworkHandler.handle(player, payload);
                    }
                })
        );
        registrar.playToClient(
                AntarchyGameResultPayload.TYPE,
                AntarchyGameResultPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> AntarchyGameClientState.update(payload))
        );
        AntarchyGravityNetworking.register(registrar);
        BrutalflyElytraNetworking.register(registrar);
        SpringyBootsNetworking.register(registrar);
        DorrieJumpNetworking.register(registrar);
        HerculesBeetleNetworking.register(registrar);
        registrar.playToClient(
                KingJudgmentFlashPayload.TYPE,
                KingJudgmentFlashPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() ->
                        KingJudgmentFlashClientState.trigger(payload.durationTicks()))
        );
        registrar.playToClient(
                BloodglassStatePayload.TYPE,
                BloodglassStatePayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() ->
                        BloodglassClientState.update(payload.shieldsActive(), payload.shieldsMax())
                )
        );
        registrar.playToClient(
                TigerEyeCamouflageStatePayload.TYPE,
                TigerEyeCamouflageStatePayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() ->
                        TigerEyeCamouflageClientState.update(payload.entityId(), payload.active(), payload.blockStateId())
                )
        );
        registrar.playToServer(
                ActivateRoyalBoundaryPayload.TYPE,
                ActivateRoyalBoundaryPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> {
                    if (ctx.player() instanceof ServerPlayer player) {
                        RoyalBoundaryManager.activate(player);
                    }
                })
        );
        registrar.playToServer(
                ToggleRoyalInversionPayload.TYPE,
                ToggleRoyalInversionPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> {
                    if (ctx.player() instanceof ServerPlayer player) {
                        RoyalAssailantArmorItem.toggleInversion(player);
                    }
                })
        );
        registrar.playToServer(
                OpenGiantFryingPanPayload.TYPE,
                OpenGiantFryingPanPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> {
                    if (ctx.player() instanceof ServerPlayer player) {
                        GiantFryingPanItem.openHeldMenu(player);
                    }
                })
        );
        registrar.playToServer(
                RoyalMountActionPayload.TYPE,
                RoyalMountActionPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> {
                    if (ctx.player() instanceof ServerPlayer mountingPlayer
                            && payload.action() == RoyalMountActionPayload.MOUNT) {
                        RoyalMountEntity.tryAssistedMount(mountingPlayer);
                        return;
                    }
                    if (!(ctx.player() instanceof ServerPlayer player)
                            || !(player.getVehicle() instanceof RoyalMountEntity mount)) {
                        return;
                    }
                    switch (payload.action()) {
                        case RoyalMountActionPayload.FLIGHT_TOGGLE -> mount.toggleMountedFlight(player);
                        case RoyalMountActionPayload.BITE -> mount.handleMountedBite(player);
                        case RoyalMountActionPayload.SPIT -> mount.handleMountedSpit(player);
                        case RoyalMountActionPayload.BEAM -> mount.handleMountedBeam(player);
                        case RoyalMountActionPayload.SIGNATURE -> mount.handleMountedSignature(player);
                        default -> {
                        }
                    }
                })
        );
        registrar.playToServer(
                RoyalMountVerticalPayload.TYPE,
                RoyalMountVerticalPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> {
                    if (ctx.player() instanceof ServerPlayer player
                            && player.getVehicle() instanceof RoyalMountEntity mount) {
                        mount.setRiderAscend(payload.ascend());
                        mount.setRiderDescend(payload.descend());
                    }
                })
        );
        registrar.playToServer(
                ToggleTigerEyeCamouflagePayload.TYPE,
                ToggleTigerEyeCamouflagePayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> {
                    if (!(ctx.player() instanceof ServerPlayer player)) {
                        return;
                    }
                    TigerEyeCamouflageController.ToggleResult result = TigerEyeCamouflageController.toggle(player);
                    if (result != TigerEyeCamouflageController.ToggleResult.NO_CHANGE) {
                        PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                                player,
                                TigerEyeCamouflageSync.payload(player)
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
                        player.sendOverlayMessage(Component.translatable(messageKey));
                    }
                })
        );
        registrar.playToClient(
                BloodCrystalKatanaTrailPayload.TYPE,
                BloodCrystalKatanaTrailPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() ->
                        BloodCrystalKatanaTrailClientState.trigger(payload.entityId(), payload.durationTicks())
                )
        );
        registrar.playToClient(
                ScorpionWhipTetherPayload.TYPE,
                ScorpionWhipTetherPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() ->
                        ScorpionWhipTetherClientState.update(payload.playerId(), payload.targetId())
                )
        );
        registrar.playToClient(
                WormHookTetherPayload.TYPE,
                WormHookTetherPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() ->
                        WormHookTetherClientState.update(payload.playerId(), payload.hookId())
                )
        );
        registrar.playToClient(
                HordeIntensityPayload.TYPE,
                HordeIntensityPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() ->
                        HordeClientState.update(payload.intensity())
                )
        );
    }
}
