package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record DorrieJumpInputPayload(boolean pressing) implements CustomPacketPayload {
    public static final Type<DorrieJumpInputPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "dorrie_jump_input"));
    public static final StreamCodec<ByteBuf, DorrieJumpInputPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, DorrieJumpInputPayload::pressing,
                    DorrieJumpInputPayload::new
            );

    @Override
    public Type<DorrieJumpInputPayload> type() {
        return TYPE;
    }
}
