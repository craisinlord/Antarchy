package com.craisinlord.antarchy.neoforge.registry;

import com.craisinlord.antarchy.content.network.BloodCrystalKatanaTrailPayload;
import com.craisinlord.antarchy.content.network.ToggleTigerEyeCamouflagePayload;
import com.craisinlord.antarchy.content.item.TigerEyeArmorUtil;
import com.craisinlord.antarchy.content.tigereye.TigerEyeCamouflageController;
import com.craisinlord.antarchy.neoforge.network.AntarchyGravityNetworking;
import com.craisinlord.antarchy.neoforge.network.BrutalflyElytraNetworking;
import com.craisinlord.antarchy.neoforge.network.DorrieJumpNetworking;
import com.craisinlord.antarchy.neoforge.network.HerculesBeetleNetworking;
import com.craisinlord.antarchy.neoforge.network.JumpyBootsNetworking;
import com.craisinlord.antarchy.neoforge.network.MultipartNetworking;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public class AntarchyNeoforgePayloadHandlers {
    private AntarchyNeoforgePayloadHandlers() {}

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(AntarchyNeoforgePayloadHandlers::registerPayloadHandlers);
    }

    static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");
        AntarchyGravityNetworking.register(registrar);
        BrutalflyElytraNetworking.register(registrar);
        JumpyBootsNetworking.register(registrar);
        DorrieJumpNetworking.register(registrar);
        HerculesBeetleNetworking.register(registrar);
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
                ToggleTigerEyeCamouflagePayload.TYPE,
                ToggleTigerEyeCamouflagePayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> {
                    if (!(ctx.player() instanceof net.minecraft.server.level.ServerPlayer player)) {
                        return;
                    }
                    TigerEyeCamouflageController.ToggleResult result = TigerEyeCamouflageController.toggle(player);
                    if (result != TigerEyeCamouflageController.ToggleResult.NO_CHANGE) {
                        net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(
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
        MultipartNetworking.register(registrar);
        // ThoraxisWeatherNetworking.register(registrar);
    }
}
