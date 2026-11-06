package com.craisinlord.antarchy.neoforge;

import com.craisinlord.antarchy.content.network.AntarchyNetwork;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

final class AntarchyNeoforgeNetworkBridge implements AntarchyNetwork.ServerBridge {
    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    @Override
    public void sendToTrackingAndSelf(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, payload);
    }
}
