package com.craisinlord.antarchy.forge.network;

import com.craisinlord.anthology.network.AnthologyNetwork;
import com.craisinlord.anthology.network.CustomPacketPayload;
import com.craisinlord.anthology.network.StreamCodec;
import java.util.function.BiConsumer;
import net.minecraft.network.FriendlyByteBuf;

public final class AntarchyForgePayloadRegistrar {
    public <T extends CustomPacketPayload> AntarchyForgePayloadRegistrar playToServer(
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super FriendlyByteBuf, T> codec,
            BiConsumer<T, AntarchyPayloadContext> handler
    ) {
        AnthologyNetwork.registerServerbound(type, codec, (payload, player) -> handler.accept(payload, new AntarchyPayloadContext(player)));
        return this;
    }

    public <T extends CustomPacketPayload> AntarchyForgePayloadRegistrar playToClient(
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super FriendlyByteBuf, T> codec,
            BiConsumer<T, AntarchyPayloadContext> handler
    ) {
        AnthologyNetwork.registerClientbound(type, codec, (payload, player) -> handler.accept(payload, new AntarchyPayloadContext(player)));
        return this;
    }
}
