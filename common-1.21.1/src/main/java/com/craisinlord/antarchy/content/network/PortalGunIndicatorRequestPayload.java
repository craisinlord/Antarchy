package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PortalGunIndicatorRequestPayload(UUID gunId) implements CustomPacketPayload {
    public static final Type<PortalGunIndicatorRequestPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "portal_gun_indicator_request"));
    public static final StreamCodec<ByteBuf, PortalGunIndicatorRequestPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeLong(payload.gunId().getMostSignificantBits());
                buffer.writeLong(payload.gunId().getLeastSignificantBits());
            },
            buffer -> new PortalGunIndicatorRequestPayload(new UUID(buffer.readLong(), buffer.readLong()))
    );

    @Override
    public Type<PortalGunIndicatorRequestPayload> type() {
        return TYPE;
    }
}
