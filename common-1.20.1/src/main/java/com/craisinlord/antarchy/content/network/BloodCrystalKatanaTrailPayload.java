package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.anthology.network.ByteBufCodecs;
import com.craisinlord.anthology.network.CustomPacketPayload;
import com.craisinlord.anthology.network.StreamCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.resources.ResourceLocation;

public record BloodCrystalKatanaTrailPayload(int entityId, int durationTicks) implements CustomPacketPayload {
    public static final Type<BloodCrystalKatanaTrailPayload> TYPE =
            new Type<>(new ResourceLocation(Antarchy.MODID, "blood_crystal_katana_trail"));
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
