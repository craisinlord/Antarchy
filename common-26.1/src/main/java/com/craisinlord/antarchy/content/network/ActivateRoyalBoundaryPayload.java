package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ActivateRoyalBoundaryPayload() implements CustomPacketPayload {
    public static final Type<ActivateRoyalBoundaryPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "activate_royal_boundary"));
    public static final StreamCodec<ByteBuf, ActivateRoyalBoundaryPayload> STREAM_CODEC = StreamCodec.unit(new ActivateRoyalBoundaryPayload());

    @Override
    public Type<ActivateRoyalBoundaryPayload> type() {
        return TYPE;
    }
}
