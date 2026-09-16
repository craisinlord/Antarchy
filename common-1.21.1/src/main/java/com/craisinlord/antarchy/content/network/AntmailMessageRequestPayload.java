package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record AntmailMessageRequestPayload(BlockPos pos, String messageId) implements CustomPacketPayload {
    public static final Type<AntmailMessageRequestPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "antmail_message_request"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AntmailMessageRequestPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, AntmailMessageRequestPayload::pos,
            ByteBufCodecs.stringUtf8(64), AntmailMessageRequestPayload::messageId,
            AntmailMessageRequestPayload::new
    );

    @Override
    public Type<AntmailMessageRequestPayload> type() {
        return TYPE;
    }
}
