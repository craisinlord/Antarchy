package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.anthology.network.CustomPacketPayload;
import com.craisinlord.anthology.network.StreamCodec;
import io.netty.buffer.ByteBuf;
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
