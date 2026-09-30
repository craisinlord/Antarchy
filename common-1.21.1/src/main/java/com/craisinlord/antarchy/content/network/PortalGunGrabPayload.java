package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PortalGunGrabPayload() implements CustomPacketPayload {
    public static final Type<PortalGunGrabPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "portal_gun_grab"));
    public static final StreamCodec<ByteBuf, PortalGunGrabPayload> STREAM_CODEC = StreamCodec.unit(new PortalGunGrabPayload());

    @Override
    public Type<PortalGunGrabPayload> type() {
        return TYPE;
    }
}
