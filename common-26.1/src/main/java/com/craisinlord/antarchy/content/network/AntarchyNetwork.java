package com.craisinlord.antarchy.content.network;

import java.util.function.Consumer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public final class AntarchyNetwork {
    public interface ServerBridge {
        void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);

        void sendToTrackingAndSelf(ServerPlayer player, CustomPacketPayload payload);
    }

    private static ServerBridge serverBridge;
    private static Consumer<CustomPacketPayload> clientBridge;

    private AntarchyNetwork() {
    }

    public static void bindServer(ServerBridge bridge) {
        serverBridge = bridge;
    }

    public static void bindClient(Consumer<CustomPacketPayload> bridge) {
        clientBridge = bridge;
    }

    public static void verifyServerBound() {
        requireServer();
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        requireServer().sendToPlayer(player, payload);
    }

    public static void sendToTrackingAndSelf(ServerPlayer player, CustomPacketPayload payload) {
        requireServer().sendToTrackingAndSelf(player, payload);
    }

    public static void sendToServer(CustomPacketPayload payload) {
        if (clientBridge == null) {
            throw new IllegalStateException("Antarchy client network bridge is not bound");
        }
        clientBridge.accept(payload);
    }

    private static ServerBridge requireServer() {
        if (serverBridge == null) {
            throw new IllegalStateException("Antarchy server network bridge is not bound");
        }
        return serverBridge;
    }
}
