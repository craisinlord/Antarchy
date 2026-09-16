package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record AntmailResultPayload(BlockPos pos, int status, String address, String messageId, String detail, String data, long version) implements CustomPacketPayload {
    public static final Type<AntmailResultPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "antmail_result"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AntmailResultPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                BlockPos.STREAM_CODEC.encode(buf, payload.pos());
                ByteBufCodecs.VAR_INT.encode(buf, payload.status());
                ByteBufCodecs.stringUtf8(64).encode(buf, payload.address());
                ByteBufCodecs.stringUtf8(64).encode(buf, payload.messageId());
                ByteBufCodecs.stringUtf8(128).encode(buf, payload.detail());
                ByteBufCodecs.stringUtf8(65536).encode(buf, payload.data());
                ByteBufCodecs.VAR_LONG.encode(buf, payload.version());
            },
            buf -> new AntmailResultPayload(
                    BlockPos.STREAM_CODEC.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.stringUtf8(64).decode(buf),
                    ByteBufCodecs.stringUtf8(64).decode(buf),
                    ByteBufCodecs.stringUtf8(128).decode(buf),
                    ByteBufCodecs.stringUtf8(65536).decode(buf),
                    ByteBufCodecs.VAR_LONG.decode(buf)
            ));
    @Override public Type<AntmailResultPayload> type() { return TYPE; }
}
