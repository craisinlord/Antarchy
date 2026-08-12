package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import com.craisinlord.antarchy.compat.network.ByteBufCodecs;
import com.craisinlord.antarchy.compat.network.StreamCodec;
import com.craisinlord.antarchy.compat.network.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record HordeIntensityPayload(float intensity) implements CustomPacketPayload {
    public static final Type<HordeIntensityPayload> TYPE =
            new Type<>(new ResourceLocation(Antarchy.MODID, "horde_intensity"));
    public static final StreamCodec<ByteBuf, HordeIntensityPayload> STREAM_CODEC =
            ByteBufCodecs.FLOAT.map(HordeIntensityPayload::new, HordeIntensityPayload::intensity);

    @Override
    public Type<HordeIntensityPayload> type() {
        return TYPE;
    }
}
