package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PortalGunIndicatorPayload(UUID gunId, boolean blueAvailable, boolean orangeAvailable) implements CustomPacketPayload {
    public static final Type<PortalGunIndicatorPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "portal_gun_indicator"));
    public static final StreamCodec<ByteBuf, PortalGunIndicatorPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeLong(payload.gunId().getMostSignificantBits());
                buffer.writeLong(payload.gunId().getLeastSignificantBits());
                buffer.writeBoolean(payload.blueAvailable());
                buffer.writeBoolean(payload.orangeAvailable());
            },
            buffer -> new PortalGunIndicatorPayload(new UUID(buffer.readLong(), buffer.readLong()), buffer.readBoolean(), buffer.readBoolean())
    );

    @Override
    public Type<PortalGunIndicatorPayload> type() {
        return TYPE;
    }
}
