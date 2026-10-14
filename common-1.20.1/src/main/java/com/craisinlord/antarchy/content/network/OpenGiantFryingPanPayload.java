package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import com.craisinlord.anthology.network.StreamCodec;
import com.craisinlord.anthology.network.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenGiantFryingPanPayload() implements CustomPacketPayload {
    public static final OpenGiantFryingPanPayload INSTANCE = new OpenGiantFryingPanPayload();
    public static final Type<OpenGiantFryingPanPayload> TYPE =
            new Type<>(new ResourceLocation(Antarchy.MODID, "open_giant_frying_pan"));
    public static final StreamCodec<ByteBuf, OpenGiantFryingPanPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<OpenGiantFryingPanPayload> type() {
        return TYPE;
    }
}
