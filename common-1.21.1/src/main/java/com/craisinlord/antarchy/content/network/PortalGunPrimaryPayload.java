package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PortalGunPrimaryPayload() implements CustomPacketPayload {
    public static final Type<PortalGunPrimaryPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "portal_gun_primary"));
    public static final StreamCodec<ByteBuf, PortalGunPrimaryPayload> STREAM_CODEC = StreamCodec.unit(new PortalGunPrimaryPayload());

    @Override
    public Type<PortalGunPrimaryPayload> type() {
        return TYPE;
    }
}
