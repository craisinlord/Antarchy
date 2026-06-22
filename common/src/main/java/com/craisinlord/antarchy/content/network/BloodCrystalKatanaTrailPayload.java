package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record BloodCrystalKatanaTrailPayload(int entityId, int durationTicks) implements CustomPacketPayload {
    public static final Type<BloodCrystalKatanaTrailPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "blood_crystal_katana_trail"));
    public static final StreamCodec<ByteBuf, BloodCrystalKatanaTrailPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, BloodCrystalKatanaTrailPayload::entityId,
            ByteBufCodecs.VAR_INT, BloodCrystalKatanaTrailPayload::durationTicks,
            BloodCrystalKatanaTrailPayload::new
    );

    @Override
    public Type<BloodCrystalKatanaTrailPayload> type() {
        return TYPE;
    }
}
