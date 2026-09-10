package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import com.craisinlord.antarchy.compat.network.StreamCodec;
import com.craisinlord.antarchy.compat.network.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record EyeOfStormPrimaryPayload() implements CustomPacketPayload {
    public static final Type<EyeOfStormPrimaryPayload> TYPE = new Type<>(new ResourceLocation(Antarchy.MODID, "eye_of_storm_primary"));
    public static final StreamCodec<ByteBuf, EyeOfStormPrimaryPayload> STREAM_CODEC = StreamCodec.unit(new EyeOfStormPrimaryPayload());

    @Override
    public Type<EyeOfStormPrimaryPayload> type() {
        return TYPE;
    }
}
