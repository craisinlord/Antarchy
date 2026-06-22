package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record GravityGunPrimaryPayload() implements CustomPacketPayload {
    public static final Type<GravityGunPrimaryPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "gravity_gun_primary"));
    public static final StreamCodec<ByteBuf, GravityGunPrimaryPayload> STREAM_CODEC = StreamCodec.unit(new GravityGunPrimaryPayload());

    @Override
    public Type<GravityGunPrimaryPayload> type() {
        return TYPE;
    }
}
