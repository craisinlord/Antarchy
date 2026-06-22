package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record BrutalflyElytraAnimationPayload(int entityId, int durationTicks, float strength) implements CustomPacketPayload {
    public static final Type<BrutalflyElytraAnimationPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "brutalfly_elytra_animation"));
    public static final StreamCodec<ByteBuf, BrutalflyElytraAnimationPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, BrutalflyElytraAnimationPayload::entityId,
            ByteBufCodecs.VAR_INT, BrutalflyElytraAnimationPayload::durationTicks,
            ByteBufCodecs.FLOAT, BrutalflyElytraAnimationPayload::strength,
            BrutalflyElytraAnimationPayload::new
    );

    @Override
    public Type<BrutalflyElytraAnimationPayload> type() {
        return TYPE;
    }
}
