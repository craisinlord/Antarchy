package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record TemporalTunerScrollPayload(double delta) implements CustomPacketPayload {
    public static final Type<TemporalTunerScrollPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "temporal_tuner_scroll"));
    public static final StreamCodec<ByteBuf, TemporalTunerScrollPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, TemporalTunerScrollPayload::delta, TemporalTunerScrollPayload::new);

    @Override
    public Type<TemporalTunerScrollPayload> type() {
        return TYPE;
    }
}
