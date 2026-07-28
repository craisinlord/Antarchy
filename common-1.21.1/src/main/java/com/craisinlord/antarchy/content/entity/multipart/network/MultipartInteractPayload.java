package com.craisinlord.antarchy.content.entity.multipart.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record MultipartInteractPayload(UUID parentId, int partIndex, int handId) implements CustomPacketPayload {
    public static final Type<MultipartInteractPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "multipart_interact"));
    public static final StreamCodec<ByteBuf, MultipartInteractPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString),
            MultipartInteractPayload::parentId,
            ByteBufCodecs.VAR_INT,
            MultipartInteractPayload::partIndex,
            ByteBufCodecs.VAR_INT,
            MultipartInteractPayload::handId,
            MultipartInteractPayload::new
    );

    @Override
    public Type<MultipartInteractPayload> type() {
        return TYPE;
    }
}
