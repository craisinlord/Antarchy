package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import com.craisinlord.anthology.network.ByteBufCodecs;
import com.craisinlord.anthology.network.StreamCodec;
import com.craisinlord.anthology.network.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ElythiaEchoTestPayload(boolean immediate) implements CustomPacketPayload {
    public static final Type<ElythiaEchoTestPayload> TYPE =
            new Type<>(new ResourceLocation(Antarchy.MODID, "elythia_echo_test"));
    public static final StreamCodec<ByteBuf, ElythiaEchoTestPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ElythiaEchoTestPayload::immediate,
            ElythiaEchoTestPayload::new
    );

    @Override
    public Type<ElythiaEchoTestPayload> type() {
        return TYPE;
    }
}
