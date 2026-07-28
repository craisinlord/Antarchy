package com.craisinlord.antarchy.forge.network;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.compat.network.CustomPacketPayload;
import com.craisinlord.antarchy.compat.network.StreamCodec;
import io.netty.buffer.ByteBuf;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class AntarchyForgeNetworkCore {
    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(Antarchy.MODID, "main"))
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .simpleChannel();

    private static int nextId = 0;

    private AntarchyForgeNetworkCore() {
    }

    public static <T extends CustomPacketPayload> void registerS2C(Class<T> clazz, StreamCodec<ByteBuf, T> codec, java.util.function.Consumer<T> clientHandler) {
        CHANNEL.registerMessage(
                nextId++,
                clazz,
                (msg, buf) -> codec.encode(buf, msg),
                codec::decode,
                (msg, ctxSupplier) -> {
                    NetworkEvent.Context ctx = ctxSupplier.get();
                    ctx.enqueueWork(() -> clientHandler.accept(msg));
                    ctx.setPacketHandled(true);
                },
                java.util.Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
    }

    public static <T extends CustomPacketPayload> void registerC2S(Class<T> clazz, StreamCodec<ByteBuf, T> codec, BiConsumer<ServerPlayer, T> serverHandler) {
        CHANNEL.registerMessage(
                nextId++,
                clazz,
                (msg, buf) -> codec.encode(buf, msg),
                codec::decode,
                (msg, ctxSupplier) -> {
                    NetworkEvent.Context ctx = ctxSupplier.get();
                    ServerPlayer player = ctx.getSender();
                    ctx.enqueueWork(() -> {
                        if (player != null) {
                            serverHandler.accept(player, msg);
                        }
                    });
                    ctx.setPacketHandled(true);
                },
                java.util.Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
    }

    public static <T> void sendToPlayer(ServerPlayer player, T payload) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), payload);
    }

    public static <T> void sendToTracking(ServerPlayer player, T payload) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(player::getVehicle), payload);
    }

    public static <T> void sendToTrackingEntity(net.minecraft.world.entity.Entity entity, T payload) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), payload);
    }

    public static <T> void sendToServer(T payload) {
        CHANNEL.sendToServer(payload);
    }
}
