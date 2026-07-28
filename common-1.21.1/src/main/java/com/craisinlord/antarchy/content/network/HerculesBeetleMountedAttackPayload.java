package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record HerculesBeetleMountedAttackPayload() implements CustomPacketPayload {
    public static final Type<HerculesBeetleMountedAttackPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "hercules_beetle_mounted_attack"));
    public static final StreamCodec<ByteBuf, HerculesBeetleMountedAttackPayload> STREAM_CODEC =
            StreamCodec.unit(new HerculesBeetleMountedAttackPayload());

    @Override
    public Type<HerculesBeetleMountedAttackPayload> type() {
        return TYPE;
    }
}
