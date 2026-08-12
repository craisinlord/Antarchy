package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import com.craisinlord.antarchy.compat.network.ByteBufCodecs;
import com.craisinlord.antarchy.compat.network.StreamCodec;
import com.craisinlord.antarchy.compat.network.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ImpactShakePayload(double x, double y, double z, float intensity, int durationTicks, float radius) implements CustomPacketPayload {
    public static final Type<ImpactShakePayload> TYPE =
            new Type<>(new ResourceLocation(Antarchy.MODID, "impact_shake"));
    public static final StreamCodec<ByteBuf, ImpactShakePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE,
            ImpactShakePayload::x,
            ByteBufCodecs.DOUBLE,
            ImpactShakePayload::y,
            ByteBufCodecs.DOUBLE,
            ImpactShakePayload::z,
            ByteBufCodecs.FLOAT,
            ImpactShakePayload::intensity,
            ByteBufCodecs.VAR_INT,
            ImpactShakePayload::durationTicks,
            ByteBufCodecs.FLOAT,
            ImpactShakePayload::radius,
            ImpactShakePayload::new
    );

    @Override
    public Type<ImpactShakePayload> type() {
        return TYPE;
    }
}
