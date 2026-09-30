package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PortalGunGrabStatePayload(boolean active) implements CustomPacketPayload {
    public static final Type<PortalGunGrabStatePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "portal_gun_grab_state"));
    public static final StreamCodec<ByteBuf, PortalGunGrabStatePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, PortalGunGrabStatePayload::active, PortalGunGrabStatePayload::new
    );

    @Override
    public Type<PortalGunGrabStatePayload> type() {
        return TYPE;
    }
}
