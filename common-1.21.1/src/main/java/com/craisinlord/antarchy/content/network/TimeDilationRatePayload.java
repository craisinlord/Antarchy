package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record TimeDilationRatePayload(int entityId, double rate) implements CustomPacketPayload {
    public static final Type<TimeDilationRatePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "time_dilation_rate"));
    public static final StreamCodec<ByteBuf, TimeDilationRatePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            TimeDilationRatePayload::entityId,
            ByteBufCodecs.DOUBLE,
            TimeDilationRatePayload::rate,
            TimeDilationRatePayload::new
    );

    @Override
    public Type<TimeDilationRatePayload> type() {
        return TYPE;
    }
}
