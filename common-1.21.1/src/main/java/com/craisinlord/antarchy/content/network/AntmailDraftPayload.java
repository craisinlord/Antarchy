package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record AntmailDraftPayload(BlockPos pos, int action, String data) implements CustomPacketPayload {
    public static final int SAVE = 0;
    public static final int DELETE = 1;
    public static final Type<AntmailDraftPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "antmail_draft"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AntmailDraftPayload> STREAM_CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC, AntmailDraftPayload::pos, ByteBufCodecs.VAR_INT, AntmailDraftPayload::action, ByteBufCodecs.stringUtf8(65536), AntmailDraftPayload::data, AntmailDraftPayload::new);
    @Override public Type<AntmailDraftPayload> type() { return TYPE; }
}
