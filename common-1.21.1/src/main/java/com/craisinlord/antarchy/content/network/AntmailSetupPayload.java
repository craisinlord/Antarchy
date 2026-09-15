package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record AntmailSetupPayload(BlockPos pos, String username) implements CustomPacketPayload {
    public static final Type<AntmailSetupPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "antmail_setup"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AntmailSetupPayload> STREAM_CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC, AntmailSetupPayload::pos, ByteBufCodecs.stringUtf8(32), AntmailSetupPayload::username, AntmailSetupPayload::new);
    @Override public Type<AntmailSetupPayload> type() { return TYPE; }
}
