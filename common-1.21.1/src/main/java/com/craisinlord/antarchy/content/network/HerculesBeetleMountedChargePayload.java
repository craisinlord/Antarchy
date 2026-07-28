package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record HerculesBeetleMountedChargePayload(boolean pressing) implements CustomPacketPayload {
    public static final Type<HerculesBeetleMountedChargePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "hercules_beetle_mounted_charge"));
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
