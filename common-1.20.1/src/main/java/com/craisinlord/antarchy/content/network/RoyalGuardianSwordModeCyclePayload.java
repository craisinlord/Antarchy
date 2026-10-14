package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import com.craisinlord.anthology.network.StreamCodec;
import com.craisinlord.anthology.network.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RoyalGuardianSwordModeCyclePayload() implements CustomPacketPayload {
    public static final Type<RoyalGuardianSwordModeCyclePayload> TYPE = new Type<>(
            new ResourceLocation(Antarchy.MODID, "royal_guardian_sword_mode_cycle"));
    public static final StreamCodec<ByteBuf, RoyalGuardianSwordModeCyclePayload> STREAM_CODEC =
            StreamCodec.unit(new RoyalGuardianSwordModeCyclePayload());

    @Override
    public Type<RoyalGuardianSwordModeCyclePayload> type() {
        return TYPE;
    }
}
