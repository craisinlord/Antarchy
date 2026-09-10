package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import com.craisinlord.antarchy.compat.network.ByteBufCodecs;
import com.craisinlord.antarchy.compat.network.StreamCodec;
import com.craisinlord.antarchy.compat.network.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RoyalMountActionPayload(int action) implements CustomPacketPayload {
    public static final int FLIGHT_TOGGLE = 0;
    public static final int BITE = 1;
    public static final int SPIT = 2;

    public static final Type<RoyalMountActionPayload> TYPE =
            new Type<>(new ResourceLocation(Antarchy.MODID, "royal_mount_action"));
    public static final StreamCodec<ByteBuf, RoyalMountActionPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, RoyalMountActionPayload::action,
            RoyalMountActionPayload::new
    );

    @Override
    public Type<RoyalMountActionPayload> type() {
        return TYPE;
    }
}
