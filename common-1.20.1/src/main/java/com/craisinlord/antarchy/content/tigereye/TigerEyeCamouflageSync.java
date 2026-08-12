package com.craisinlord.antarchy.content.tigereye;

import com.craisinlord.antarchy.content.network.TigerEyeCamouflageStatePayload;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.server.level.ServerPlayer;

public final class TigerEyeCamouflageSync {
    private static BiConsumer<ServerPlayer, TigerEyeCamouflageStatePayload> sendToPlayer = (player, payload) -> {};
    private static Consumer<ServerPlayer> syncSelfAndTracking = player -> {};

    private TigerEyeCamouflageSync() {
    }

    public static void setSendToPlayer(BiConsumer<ServerPlayer, TigerEyeCamouflageStatePayload> sender) {
        sendToPlayer = sender;
    }

    public static void setSyncSelfAndTracking(Consumer<ServerPlayer> syncer) {
        syncSelfAndTracking = syncer;
    }

    public static void sync(ServerPlayer player) {
        syncSelfAndTracking.accept(player);
    }

    public static void syncTo(ServerPlayer viewer, ServerPlayer target) {
        sendToPlayer.accept(viewer, payload(target));
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
