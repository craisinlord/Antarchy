package com.craisinlord.antarchy.forge.network;

import com.craisinlord.anthology.network.AnthologyNetwork;
import com.craisinlord.anthology.network.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public final class AntarchyForgeNetworkCore {
    private AntarchyForgeNetworkCore() {
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        AnthologyNetwork.sendToPlayer(player, payload);
    }

    public static void sendToTrackingEntity(Entity entity, CustomPacketPayload payload) {
        AnthologyNetwork.sendToTrackingAndSelf(entity, payload);
    }

    public static void sendToServer(CustomPacketPayload payload) {
        AnthologyNetwork.sendToServer(payload);
    }
}
