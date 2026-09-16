package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record AntmailRetryPayload(BlockPos pos, String messageId) implements CustomPacketPayload {
    public static final Type<AntmailRetryPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "antmail_retry"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AntmailRetryPayload> STREAM_CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC, AntmailRetryPayload::pos, ByteBufCodecs.stringUtf8(64), AntmailRetryPayload::messageId, AntmailRetryPayload::new);
    @Override public Type<AntmailRetryPayload> type() { return TYPE; }
}
