package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.weather.ThoraxisWeatherKind;
import io.netty.buffer.ByteBuf;
import com.craisinlord.antarchy.compat.network.ByteBufCodecs;
import com.craisinlord.antarchy.compat.network.StreamCodec;
import com.craisinlord.antarchy.compat.network.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ThoraxisWeatherPayload(
        String dimensionId,
        String weatherId,
        long expiresAt,
        int anchorX,
        int anchorY,
        int anchorZ
) implements CustomPacketPayload {
    public static final Type<ThoraxisWeatherPayload> TYPE =
            new Type<>(new ResourceLocation(Antarchy.MODID, "thoraxis_weather"));

    public static final StreamCodec<ByteBuf, ThoraxisWeatherPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            ThoraxisWeatherPayload::dimensionId,
            ByteBufCodecs.STRING_UTF8,
            ThoraxisWeatherPayload::weatherId,
            ByteBufCodecs.VAR_LONG,
            ThoraxisWeatherPayload::expiresAt,
            ByteBufCodecs.VAR_INT,
            ThoraxisWeatherPayload::anchorX,
            ByteBufCodecs.VAR_INT,
            ThoraxisWeatherPayload::anchorY,
            ByteBufCodecs.VAR_INT,
            ThoraxisWeatherPayload::anchorZ,
            ThoraxisWeatherPayload::new
    );

    public ThoraxisWeatherPayload(ResourceLocation dimensionId, ThoraxisWeatherKind kind, long expiresAt, int anchorX, int anchorY, int anchorZ) {
        this(dimensionId.toString(), kind.id(), expiresAt, anchorX, anchorY, anchorZ);
    }

    @Override
    public Type<ThoraxisWeatherPayload> type() {
        return TYPE;
    }
}
