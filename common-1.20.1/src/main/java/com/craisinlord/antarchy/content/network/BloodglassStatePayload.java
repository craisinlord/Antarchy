package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import com.craisinlord.antarchy.compat.network.ByteBufCodecs;
import com.craisinlord.antarchy.compat.network.StreamCodec;
import com.craisinlord.antarchy.compat.network.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record BloodglassStatePayload(int shieldsActive, int shieldsMax) implements CustomPacketPayload {
    public static final Type<BloodglassStatePayload> TYPE =
            new Type<>(new ResourceLocation(Antarchy.MODID, "bloodglass_state"));
    public static final StreamCodec<ByteBuf, BloodglassStatePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, BloodglassStatePayload::shieldsActive,
            ByteBufCodecs.VAR_INT, BloodglassStatePayload::shieldsMax,
            BloodglassStatePayload::new
    );

    @Override
    public Type<BloodglassStatePayload> type() {
        return TYPE;
    }
}
