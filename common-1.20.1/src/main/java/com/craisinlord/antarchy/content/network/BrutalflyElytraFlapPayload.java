package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import com.craisinlord.anthology.network.ByteBufCodecs;
import com.craisinlord.anthology.network.StreamCodec;
import com.craisinlord.anthology.network.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record BrutalflyElytraFlapPayload(int chargeTicks) implements CustomPacketPayload {
    public static final Type<BrutalflyElytraFlapPayload> TYPE =
            new Type<>(new ResourceLocation(Antarchy.MODID, "brutalfly_elytra_flap"));
    public static final StreamCodec<ByteBuf, BrutalflyElytraFlapPayload> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(BrutalflyElytraFlapPayload::new, BrutalflyElytraFlapPayload::chargeTicks);

    @Override
    public Type<BrutalflyElytraFlapPayload> type() {
        return TYPE;
    }
}
