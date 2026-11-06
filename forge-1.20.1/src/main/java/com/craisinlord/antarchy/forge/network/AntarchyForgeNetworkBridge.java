package com.craisinlord.antarchy.forge.network;

import com.craisinlord.antarchy.content.network.AntarchyNetwork;
import com.craisinlord.anthology.network.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public final class AntarchyForgeNetworkBridge implements AntarchyNetwork.ServerBridge {
    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        AntarchyForgeNetworkCore.sendToPlayer(player, payload);
    }

    @Override
    public void sendToTrackingAndSelf(ServerPlayer player, CustomPacketPayload payload) {
        AntarchyForgeNetworkCore.sendToTrackingEntity(player, payload);
    }
}
