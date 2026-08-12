package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import com.craisinlord.antarchy.compat.network.ByteBufCodecs;
import com.craisinlord.antarchy.compat.network.StreamCodec;
import com.craisinlord.antarchy.compat.network.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record HerculesBeetleMountedChargePayload(boolean pressing) implements CustomPacketPayload {
    public static final Type<HerculesBeetleMountedChargePayload> TYPE =
            new Type<>(new ResourceLocation(Antarchy.MODID, "hercules_beetle_mounted_charge"));
    public static final StreamCodec<ByteBuf, HerculesBeetleMountedChargePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, HerculesBeetleMountedChargePayload::pressing,
                    HerculesBeetleMountedChargePayload::new
            );

    @Override
    public Type<HerculesBeetleMountedChargePayload> type() {
        return TYPE;
    }
}
