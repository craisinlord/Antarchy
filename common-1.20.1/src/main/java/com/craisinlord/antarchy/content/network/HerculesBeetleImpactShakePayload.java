package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import com.craisinlord.antarchy.compat.network.ByteBufCodecs;
import com.craisinlord.antarchy.compat.network.StreamCodec;
import com.craisinlord.antarchy.compat.network.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record HerculesBeetleImpactShakePayload(int durationTicks) implements CustomPacketPayload {
    public static final Type<HerculesBeetleImpactShakePayload> TYPE =
            new Type<>(new ResourceLocation(Antarchy.MODID, "hercules_beetle_impact_shake"));
    public static final StreamCodec<ByteBuf, HerculesBeetleImpactShakePayload> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(HerculesBeetleImpactShakePayload::new, HerculesBeetleImpactShakePayload::durationTicks);

    @Override
    public Type<HerculesBeetleImpactShakePayload> type() {
        return TYPE;
    }
}
