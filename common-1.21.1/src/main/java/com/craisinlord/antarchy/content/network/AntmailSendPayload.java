package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record AntmailSendPayload(BlockPos pos, String recipient, String subject, String body, String attachments) implements CustomPacketPayload {
    public static final Type<AntmailSendPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "antmail_send"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AntmailSendPayload> STREAM_CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC, AntmailSendPayload::pos, ByteBufCodecs.stringUtf8(64), AntmailSendPayload::recipient, ByteBufCodecs.stringUtf8(64), AntmailSendPayload::subject, ByteBufCodecs.stringUtf8(16384), AntmailSendPayload::body, ByteBufCodecs.stringUtf8(65536), AntmailSendPayload::attachments, AntmailSendPayload::new);
    @Override public Type<AntmailSendPayload> type() { return TYPE; }
}
