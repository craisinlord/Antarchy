package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import com.craisinlord.antarchy.compat.network.ByteBufCodecs;
import com.craisinlord.antarchy.compat.network.StreamCodec;
import com.craisinlord.antarchy.compat.network.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record GravityGunScrollPayload(double distanceDelta) implements CustomPacketPayload {
    public static final Type<GravityGunScrollPayload> TYPE = new Type<>(new ResourceLocation(Antarchy.MODID, "gravity_gun_scroll"));
    public static final StreamCodec<ByteBuf, GravityGunScrollPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE,
            GravityGunScrollPayload::distanceDelta,
            GravityGunScrollPayload::new
    );

    @Override
    public Type<GravityGunScrollPayload> type() {
        return TYPE;
    }
}
