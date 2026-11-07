package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.anthology.network.ByteBufCodecs;
import com.craisinlord.anthology.network.CustomPacketPayload;
import com.craisinlord.anthology.network.StreamCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.resources.ResourceLocation;

public record ThrowbackZonePayload(float strength) implements CustomPacketPayload {
    public static final Type<ThrowbackZonePayload> TYPE =
            new Type<>(new ResourceLocation(Antarchy.MODID, "throwback_zone"));
    public static final StreamCodec<ByteBuf, ThrowbackZonePayload> STREAM_CODEC =
            ByteBufCodecs.FLOAT.map(ThrowbackZonePayload::new, ThrowbackZonePayload::strength);

    @Override
    public Type<ThrowbackZonePayload> type() {
        return TYPE;
    }
}
