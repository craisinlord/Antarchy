package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record AntmailDeletePayload(BlockPos pos, String messageId, boolean sent) implements CustomPacketPayload {
    public static final Type<AntmailDeletePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "antmail_delete"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AntmailDeletePayload> STREAM_CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC, AntmailDeletePayload::pos, ByteBufCodecs.stringUtf8(64), AntmailDeletePayload::messageId, ByteBufCodecs.BOOL, AntmailDeletePayload::sent, AntmailDeletePayload::new);
    @Override public Type<AntmailDeletePayload> type() { return TYPE; }
}
