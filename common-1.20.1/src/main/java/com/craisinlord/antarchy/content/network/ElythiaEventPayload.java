package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import com.craisinlord.anthology.network.ByteBufCodecs;
import com.craisinlord.anthology.network.StreamCodec;
import com.craisinlord.anthology.network.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ElythiaEventPayload(int event, long seed) implements CustomPacketPayload {
    public static final Type<ElythiaEventPayload> TYPE =
            new Type<>(new ResourceLocation(Antarchy.MODID, "elythia_event"));
    public static final StreamCodec<ByteBuf, ElythiaEventPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ElythiaEventPayload::event,
            ByteBufCodecs.VAR_LONG, ElythiaEventPayload::seed,
            ElythiaEventPayload::new
    );

    @Override
    public Type<ElythiaEventPayload> type() {
        return TYPE;
    }
}
