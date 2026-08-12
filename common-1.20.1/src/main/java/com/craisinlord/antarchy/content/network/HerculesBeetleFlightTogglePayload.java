package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import com.craisinlord.antarchy.compat.network.StreamCodec;
import com.craisinlord.antarchy.compat.network.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record HerculesBeetleFlightTogglePayload() implements CustomPacketPayload {
    public static final Type<HerculesBeetleFlightTogglePayload> TYPE =
            new Type<>(new ResourceLocation(Antarchy.MODID, "hercules_beetle_flight_toggle"));
    public static final StreamCodec<ByteBuf, HerculesBeetleFlightTogglePayload> STREAM_CODEC =
            StreamCodec.unit(new HerculesBeetleFlightTogglePayload());

    @Override
    public Type<HerculesBeetleFlightTogglePayload> type() {
        return TYPE;
    }
}
