package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record HordeIntensityPayload(float intensity) implements CustomPacketPayload {
    public static final Type<HordeIntensityPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "horde_intensity"));
    public static final StreamCodec<ByteBuf, HordeIntensityPayload> STREAM_CODEC =
            ByteBufCodecs.FLOAT.map(HordeIntensityPayload::new, HordeIntensityPayload::intensity);

    @Override
    public Type<HordeIntensityPayload> type() {
        return TYPE;
    }
}
