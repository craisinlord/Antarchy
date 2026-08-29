package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record EyeOfStormPrimaryPayload() implements CustomPacketPayload {
    public static final Type<EyeOfStormPrimaryPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "eye_of_storm_primary"));
    public static final StreamCodec<ByteBuf, EyeOfStormPrimaryPayload> STREAM_CODEC = StreamCodec.unit(new EyeOfStormPrimaryPayload());

    @Override
    public Type<EyeOfStormPrimaryPayload> type() {
        return TYPE;
    }
}
