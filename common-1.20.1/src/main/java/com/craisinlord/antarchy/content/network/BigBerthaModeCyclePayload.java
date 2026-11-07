package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.anthology.network.CustomPacketPayload;
import com.craisinlord.anthology.network.StreamCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.resources.ResourceLocation;

public record BigBerthaModeCyclePayload() implements CustomPacketPayload {
    public static final Type<BigBerthaModeCyclePayload> TYPE = new Type<>(new ResourceLocation(Antarchy.MODID, "big_bertha_mode_cycle"));
    public static final StreamCodec<ByteBuf, BigBerthaModeCyclePayload> STREAM_CODEC = StreamCodec.unit(new BigBerthaModeCyclePayload());

    @Override
    public Type<BigBerthaModeCyclePayload> type() {
        return TYPE;
    }
}
