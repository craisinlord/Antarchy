package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import com.craisinlord.anthology.network.StreamCodec;
import com.craisinlord.anthology.network.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ActivateRoyalBoundaryPayload() implements CustomPacketPayload {
    public static final Type<ActivateRoyalBoundaryPayload> TYPE = new Type<>(
            new ResourceLocation(Antarchy.MODID, "activate_royal_boundary"));
    public static final StreamCodec<ByteBuf, ActivateRoyalBoundaryPayload> STREAM_CODEC = StreamCodec.unit(new ActivateRoyalBoundaryPayload());

    @Override
    public Type<ActivateRoyalBoundaryPayload> type() {
        return TYPE;
    }
}
