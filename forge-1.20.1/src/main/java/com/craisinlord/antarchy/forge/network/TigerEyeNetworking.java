package com.craisinlord.antarchy.forge.network;

import com.craisinlord.antarchy.content.item.TigerEyeArmorUtil;
import com.craisinlord.antarchy.content.network.TigerEyeCamouflageStatePayload;
import com.craisinlord.antarchy.content.network.ToggleTigerEyeCamouflagePayload;
import com.craisinlord.antarchy.content.tigereye.TigerEyeCamouflageController;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class TigerEyeNetworking {
    private TigerEyeNetworking() {
    }

    public static void register() {
        AntarchyForgeNetworkCore.registerS2C(TigerEyeCamouflageStatePayload.class, TigerEyeCamouflageStatePayload.STREAM_CODEC, payload ->
                com.craisinlord.antarchy.content.client.TigerEyeCamouflageClientState.update(payload.entityId(), payload.active(), payload.blockStateId()));
        AntarchyForgeNetworkCore.registerC2S(ToggleTigerEyeCamouflagePayload.class, ToggleTigerEyeCamouflagePayload.STREAM_CODEC, (player, payload) -> handleToggle(player));
    }

    private static void handleToggle(ServerPlayer player) {
        TigerEyeCamouflageController.ToggleResult result = TigerEyeCamouflageController.toggle(player);
        if (result != TigerEyeCamouflageController.ToggleResult.NO_CHANGE) {
            AntarchyForgeNetworkCore.sendToTrackingEntity(player,
                    com.craisinlord.antarchy.content.tigereye.TigerEyeCamouflageSync.payload(player));
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
            player.displayClientMessage(Component.translatable(messageKey), true);
        }
    }
}
