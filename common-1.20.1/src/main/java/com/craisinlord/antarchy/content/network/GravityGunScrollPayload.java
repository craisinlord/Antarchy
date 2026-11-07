package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.anthology.network.ByteBufCodecs;
import com.craisinlord.anthology.network.CustomPacketPayload;
import com.craisinlord.anthology.network.StreamCodec;
import io.netty.buffer.ByteBuf;
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
