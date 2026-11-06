package com.craisinlord.antarchy.content.tigereye;

import com.craisinlord.antarchy.content.network.AntarchyNetwork;
import com.craisinlord.antarchy.content.network.TigerEyeCamouflageStatePayload;
import net.minecraft.server.level.ServerPlayer;

public final class TigerEyeCamouflageSync {
    private TigerEyeCamouflageSync() {
    }

    public static void sync(ServerPlayer player) {
        AntarchyNetwork.sendToTrackingAndSelf(player, payload(player));
    }

    public static void syncTo(ServerPlayer viewer, ServerPlayer target) {
        AntarchyNetwork.sendToPlayer(viewer, payload(target));
    }

    public static TigerEyeCamouflageStatePayload payload(ServerPlayer player) {
        if (player instanceof TigerEyeCamouflageAccess access) {
            return new TigerEyeCamouflageStatePayload(
                    player.getId(),
                    access.antarchy$isTigerEyeCamouflageActive(),
                    access.antarchy$getTigerEyeCamouflageBlockStateId()
            );
        }
        return new TigerEyeCamouflageStatePayload(player.getId(), false, 0);
    }
}
