package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RoyalMountActionPayload(int action) implements CustomPacketPayload {
    public static final int FLIGHT_TOGGLE = 0;
    public static final int BITE = 1;
    public static final int SPIT = 2;

    public static final Type<RoyalMountActionPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "royal_mount_action"));
    public static final StreamCodec<ByteBuf, RoyalMountActionPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, RoyalMountActionPayload::action,
            RoyalMountActionPayload::new
    );

    @Override
    public Type<RoyalMountActionPayload> type() {
        return TYPE;
    }
}
