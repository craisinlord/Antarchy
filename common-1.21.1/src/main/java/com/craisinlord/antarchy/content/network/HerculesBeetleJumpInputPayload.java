package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record HerculesBeetleJumpInputPayload(boolean pressing) implements CustomPacketPayload {
    public static final Type<HerculesBeetleJumpInputPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "hercules_beetle_jump_input"));
    public static final StreamCodec<ByteBuf, HerculesBeetleJumpInputPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, HerculesBeetleJumpInputPayload::pressing,
                    HerculesBeetleJumpInputPayload::new
            );

    @Override
    public Type<HerculesBeetleJumpInputPayload> type() {
        return TYPE;
    }
}
