package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RoyalMountVerticalPayload(boolean ascend, boolean descend) implements CustomPacketPayload {
    public static final Type<RoyalMountVerticalPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "royal_mount_vertical"));
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
