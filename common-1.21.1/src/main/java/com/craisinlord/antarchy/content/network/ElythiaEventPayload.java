package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ElythiaEventPayload(int event, long seed) implements CustomPacketPayload {
    public static final Type<ElythiaEventPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "elythia_event"));
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
