package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record DorrieChargeJumpPayload(boolean pressing) implements CustomPacketPayload {
    public static final Type<DorrieChargeJumpPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "dorrie_charge_jump_input"));
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
