package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record AntTransitPayload(int entityId, TransitStyle style, byte phase, double x, double y, double z, boolean glide)
        implements CustomPacketPayload {
    public static final byte PHASE_START = 0;
    public static final byte PHASE_ARRIVED = 1;
    public static final byte PHASE_CANCEL = 2;

    public static final Type<AntTransitPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "ant_transit"));
    public static final StreamCodec<ByteBuf, AntTransitPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeInt(payload.entityId);
                buffer.writeByte(payload.style.ordinal());
                buffer.writeByte(payload.phase);
                buffer.writeDouble(payload.x);
                buffer.writeDouble(payload.y);
                buffer.writeDouble(payload.z);
                buffer.writeBoolean(payload.glide);
            },
            buffer -> new AntTransitPayload(buffer.readInt(), TransitStyle.byId(buffer.readByte()), buffer.readByte(),
                    buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readBoolean())
    );

    @Override
    public Type<AntTransitPayload> type() {
        return TYPE;
    }
}
