package com.craisinlord.antarchy.fabric;

import com.craisinlord.antarchy.content.tigereye.TigerEyeCamouflageController;
import com.craisinlord.antarchy.content.tigereye.TigerEyeCamouflageSync;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class TigerEyeFabricManager {
    private TigerEyeFabricManager() {
    }

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> TigerEyeCamouflageSync.sync(handler.player));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                TigerEyeCamouflageController.deactivate(handler.player, false));
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            TigerEyeCamouflageController.deactivate(newPlayer, false);
            TigerEyeCamouflageSync.sync(newPlayer);
        });
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> {
            TigerEyeCamouflageController.deactivate(player, false);
            TigerEyeCamouflageSync.sync(player);
        });
        EntityTrackingEvents.START_TRACKING.register((trackedEntity, player) -> {
            if (trackedEntity instanceof ServerPlayer trackedPlayer) {
                TigerEyeCamouflageSync.syncTo(player, trackedPlayer);
            }
        });
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof ServerPlayer player) {
                TigerEyeCamouflageController.deactivate(player, false);
                TigerEyeCamouflageSync.sync(player);
            }
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerLevel level : server.getAllLevels()) {
                for (ServerPlayer player : level.players()) {
                    if (TigerEyeCamouflageController.validateOrDeactivate(player)) {
                        TigerEyeCamouflageSync.sync(player);
                    }
                }
            }
        });
    }
}
