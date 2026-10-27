package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ElythiaEchoTestPayload(boolean immediate) implements CustomPacketPayload {
    public static final Type<ElythiaEchoTestPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "elythia_echo_test"));
    public static final StreamCodec<ByteBuf, ElythiaEchoTestPayload> STREAM_CODEC =
            ByteBufCodecs.BOOL.map(ElythiaEchoTestPayload::new, ElythiaEchoTestPayload::immediate);

    @Override
    public Type<ElythiaEchoTestPayload> type() {
        return TYPE;
    }
}
