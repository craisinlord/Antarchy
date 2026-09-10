package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import com.craisinlord.antarchy.compat.network.ByteBufCodecs;
import com.craisinlord.antarchy.compat.network.StreamCodec;
import com.craisinlord.antarchy.compat.network.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RoyalMountVerticalPayload(boolean ascend, boolean descend) implements CustomPacketPayload {
    public static final Type<RoyalMountVerticalPayload> TYPE =
            new Type<>(new ResourceLocation(Antarchy.MODID, "royal_mount_vertical"));
    public static final StreamCodec<ByteBuf, RoyalMountVerticalPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, RoyalMountVerticalPayload::ascend,
            ByteBufCodecs.BOOL, RoyalMountVerticalPayload::descend,
            RoyalMountVerticalPayload::new
    );

    @Override
    public Type<RoyalMountVerticalPayload> type() {
        return TYPE;
    }
}
