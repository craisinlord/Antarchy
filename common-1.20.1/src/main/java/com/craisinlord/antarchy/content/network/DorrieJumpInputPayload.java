package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import com.craisinlord.antarchy.compat.network.ByteBufCodecs;
import com.craisinlord.antarchy.compat.network.StreamCodec;
import com.craisinlord.antarchy.compat.network.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record DorrieJumpInputPayload(boolean pressing) implements CustomPacketPayload {
    public static final Type<DorrieJumpInputPayload> TYPE =
            new Type<>(new ResourceLocation(Antarchy.MODID, "dorrie_jump_input"));
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
