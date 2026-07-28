package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record GravityGunScrollPayload(double distanceDelta) implements CustomPacketPayload {
    public static final Type<GravityGunScrollPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "gravity_gun_scroll"));
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
