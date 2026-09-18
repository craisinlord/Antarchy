package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RoyalGuardianSwordModeCyclePayload() implements CustomPacketPayload {
    public static final Type<RoyalGuardianSwordModeCyclePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "royal_guardian_sword_mode_cycle"));
    public static final StreamCodec<ByteBuf, RoyalGuardianSwordModeCyclePayload> STREAM_CODEC =
            StreamCodec.unit(new RoyalGuardianSwordModeCyclePayload());

    @Override
    public Type<RoyalGuardianSwordModeCyclePayload> type() {
        return TYPE;
    }
}
