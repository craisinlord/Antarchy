package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record AntDescentPayload(int entityId, boolean home, byte phase, double x, double y, double z, boolean glide)
        implements CustomPacketPayload {
    public static final byte PHASE_START = 0;
    public static final byte PHASE_ARRIVED = 1;
    public static final byte PHASE_CANCEL = 2;

    public static final Type<AntDescentPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "ant_descent"));
    public static final StreamCodec<ByteBuf, AntDescentPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeInt(payload.entityId);
                buffer.writeBoolean(payload.home);
                buffer.writeByte(payload.phase);
                buffer.writeDouble(payload.x);
                buffer.writeDouble(payload.y);
                buffer.writeDouble(payload.z);
                buffer.writeBoolean(payload.glide);
            },
            buffer -> new AntDescentPayload(buffer.readInt(), buffer.readBoolean(), buffer.readByte(),
                    buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readBoolean())
    );

    @Override
    public Type<AntDescentPayload> type() {
        return TYPE;
    }
}
