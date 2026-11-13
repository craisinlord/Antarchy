package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.anthology.network.ByteBufCodecs;
import com.craisinlord.anthology.network.CustomPacketPayload;
import com.craisinlord.anthology.network.StreamCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.resources.ResourceLocation;

public record TemporalTunerScrollPayload(double delta) implements CustomPacketPayload {
    public static final Type<TemporalTunerScrollPayload> TYPE = new Type<>(new ResourceLocation(Antarchy.MODID, "temporal_tuner_scroll"));
    public static final StreamCodec<ByteBuf, TemporalTunerScrollPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, TemporalTunerScrollPayload::delta, TemporalTunerScrollPayload::new);

    @Override
    public Type<TemporalTunerScrollPayload> type() {
        return TYPE;
    }
}
