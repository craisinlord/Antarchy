package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import com.craisinlord.antarchy.compat.network.StreamCodec;
import com.craisinlord.antarchy.compat.network.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ToggleRoyalInversionPayload() implements CustomPacketPayload {
    public static final Type<ToggleRoyalInversionPayload> TYPE =
            new Type<>(new ResourceLocation(Antarchy.MODID, "toggle_royal_inversion"));
    public static final StreamCodec<ByteBuf, ToggleRoyalInversionPayload> STREAM_CODEC = StreamCodec.unit(new ToggleRoyalInversionPayload());

    @Override
    public Type<ToggleRoyalInversionPayload> type() {
        return TYPE;
    }
}
