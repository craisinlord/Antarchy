package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record AntmailStateRequestPayload(BlockPos pos, int folder, int page, long knownVersion) implements CustomPacketPayload {
    public static final int INBOX = 0;
    public static final int SENT = 1;
    public static final int DRAFTS = 2;
    public static final Type<AntmailStateRequestPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "antmail_state_request"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AntmailStateRequestPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, AntmailStateRequestPayload::pos,
            ByteBufCodecs.VAR_INT, AntmailStateRequestPayload::folder,
            ByteBufCodecs.VAR_INT, AntmailStateRequestPayload::page,
            ByteBufCodecs.VAR_LONG, AntmailStateRequestPayload::knownVersion,
            AntmailStateRequestPayload::new);
    @Override public Type<AntmailStateRequestPayload> type() { return TYPE; }
}
