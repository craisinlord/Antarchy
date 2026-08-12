package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import com.craisinlord.antarchy.compat.network.ByteBufCodecs;
import com.craisinlord.antarchy.compat.network.StreamCodec;
import com.craisinlord.antarchy.compat.network.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record DorrieChargeJumpPayload(boolean pressing) implements CustomPacketPayload {
    public static final Type<DorrieChargeJumpPayload> TYPE =
            new Type<>(new ResourceLocation(Antarchy.MODID, "dorrie_charge_jump_input"));
    public static final StreamCodec<ByteBuf, DorrieChargeJumpPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, DorrieChargeJumpPayload::pressing,
                    DorrieChargeJumpPayload::new
            );

    @Override
    public Type<DorrieChargeJumpPayload> type() {
        return TYPE;
    }
}
