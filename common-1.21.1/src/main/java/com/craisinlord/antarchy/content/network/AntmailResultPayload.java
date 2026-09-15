package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record AntmailResultPayload(BlockPos pos, int status, String address, String messageId, String detail, String data) implements CustomPacketPayload {
    public static final Type<AntmailResultPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "antmail_result"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AntmailResultPayload> STREAM_CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC, AntmailResultPayload::pos, ByteBufCodecs.VAR_INT, AntmailResultPayload::status, ByteBufCodecs.stringUtf8(64), AntmailResultPayload::address, ByteBufCodecs.stringUtf8(64), AntmailResultPayload::messageId, ByteBufCodecs.stringUtf8(128), AntmailResultPayload::detail, ByteBufCodecs.stringUtf8(65536), AntmailResultPayload::data, AntmailResultPayload::new);
    @Override public Type<AntmailResultPayload> type() { return TYPE; }
}
