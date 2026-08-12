package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import com.craisinlord.antarchy.compat.network.StreamCodec;
import com.craisinlord.antarchy.compat.network.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record HerculesBeetleMountedAttackPayload() implements CustomPacketPayload {
    public static final Type<HerculesBeetleMountedAttackPayload> TYPE =
            new Type<>(new ResourceLocation(Antarchy.MODID, "hercules_beetle_mounted_attack"));
    public static final StreamCodec<ByteBuf, HerculesBeetleMountedAttackPayload> STREAM_CODEC =
            StreamCodec.unit(new HerculesBeetleMountedAttackPayload());

    @Override
    public Type<HerculesBeetleMountedAttackPayload> type() {
        return TYPE;
    }
}
