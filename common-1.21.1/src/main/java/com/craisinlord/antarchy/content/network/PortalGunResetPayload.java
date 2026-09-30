package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PortalGunResetPayload(int action) implements CustomPacketPayload {
    public static final Type<PortalGunResetPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "portal_gun_reset"));
    public static final StreamCodec<ByteBuf, PortalGunResetPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, PortalGunResetPayload::action, PortalGunResetPayload::new
    );

    @Override
    public Type<PortalGunResetPayload> type() {
        return TYPE;
    }
}
