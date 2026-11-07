package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.anthology.network.CustomPacketPayload;
import com.craisinlord.anthology.network.StreamCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.resources.ResourceLocation;

public record GravityGunPrimaryPayload() implements CustomPacketPayload {
    public static final Type<GravityGunPrimaryPayload> TYPE = new Type<>(new ResourceLocation(Antarchy.MODID, "gravity_gun_primary"));
    public static final StreamCodec<ByteBuf, GravityGunPrimaryPayload> STREAM_CODEC = StreamCodec.unit(new GravityGunPrimaryPayload());

    @Override
    public Type<GravityGunPrimaryPayload> type() {
        return TYPE;
    }
}
