package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ToggleRoyalInversionPayload() implements CustomPacketPayload {
    public static final Type<ToggleRoyalInversionPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "toggle_royal_inversion"));
    public static final StreamCodec<ByteBuf, ToggleRoyalInversionPayload> STREAM_CODEC = StreamCodec.unit(new ToggleRoyalInversionPayload());

    @Override
    public Type<ToggleRoyalInversionPayload> type() {
        return TYPE;
    }
}
