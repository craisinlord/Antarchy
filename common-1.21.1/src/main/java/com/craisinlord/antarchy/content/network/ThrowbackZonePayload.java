package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ThrowbackZonePayload(float strength) implements CustomPacketPayload {
    public static final Type<ThrowbackZonePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "throwback_zone"));
    public static final StreamCodec<ByteBuf, ThrowbackZonePayload> STREAM_CODEC =
            ByteBufCodecs.FLOAT.map(ThrowbackZonePayload::new, ThrowbackZonePayload::strength);

    @Override
    public Type<ThrowbackZonePayload> type() {
        return TYPE;
    }
}
