package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record HerculesBeetleFlightTogglePayload() implements CustomPacketPayload {
    public static final Type<HerculesBeetleFlightTogglePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "hercules_beetle_flight_toggle"));
    public static final StreamCodec<ByteBuf, HerculesBeetleFlightTogglePayload> STREAM_CODEC =
            StreamCodec.unit(new HerculesBeetleFlightTogglePayload());

    @Override
    public Type<HerculesBeetleFlightTogglePayload> type() {
        return TYPE;
    }
}
