package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import com.craisinlord.antarchy.compat.network.ByteBufCodecs;
import com.craisinlord.antarchy.compat.network.StreamCodec;
import com.craisinlord.antarchy.compat.network.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record HerculesBeetleJumpInputPayload(boolean pressing) implements CustomPacketPayload {
    public static final Type<HerculesBeetleJumpInputPayload> TYPE =
            new Type<>(new ResourceLocation(Antarchy.MODID, "hercules_beetle_jump_input"));
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
