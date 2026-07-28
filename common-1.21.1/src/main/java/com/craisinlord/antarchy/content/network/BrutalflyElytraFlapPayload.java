package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record BrutalflyElytraFlapPayload(int chargeTicks) implements CustomPacketPayload {
    public static final Type<BrutalflyElytraFlapPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "brutalfly_elytra_flap"));
    public static final StreamCodec<ByteBuf, BrutalflyElytraFlapPayload> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(BrutalflyElytraFlapPayload::new, BrutalflyElytraFlapPayload::chargeTicks);

    @Override
    public Type<BrutalflyElytraFlapPayload> type() {
        return TYPE;
    }
}
