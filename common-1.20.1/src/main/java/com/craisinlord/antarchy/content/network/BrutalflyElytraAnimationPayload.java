package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import com.craisinlord.antarchy.compat.network.ByteBufCodecs;
import com.craisinlord.antarchy.compat.network.StreamCodec;
import com.craisinlord.antarchy.compat.network.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record BrutalflyElytraAnimationPayload(int entityId, int durationTicks, float strength) implements CustomPacketPayload {
    public static final Type<BrutalflyElytraAnimationPayload> TYPE =
            new Type<>(new ResourceLocation(Antarchy.MODID, "brutalfly_elytra_animation"));
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
