package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record BigBerthaModeCyclePayload() implements CustomPacketPayload {
    public static final Type<BigBerthaModeCyclePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Antarchy.MODID, "big_bertha_mode_cycle"));
    public static final StreamCodec<ByteBuf, BigBerthaModeCyclePayload> STREAM_CODEC = StreamCodec.unit(new BigBerthaModeCyclePayload());

    @Override
    public Type<BigBerthaModeCyclePayload> type() {
        return TYPE;
    }
}
