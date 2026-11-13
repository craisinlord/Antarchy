package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.anthology.network.CustomPacketPayload;
import com.craisinlord.anthology.network.StreamCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.resources.ResourceLocation;

public record ToggleTigerEyeCamouflagePayload() implements CustomPacketPayload {
    public static final Type<ToggleTigerEyeCamouflagePayload> TYPE =
            new Type<>(new ResourceLocation(Antarchy.MODID, "toggle_tiger_eye_camouflage"));
    public static final StreamCodec<ByteBuf, ToggleTigerEyeCamouflagePayload> STREAM_CODEC =
            StreamCodec.unit(new ToggleTigerEyeCamouflagePayload());

    @Override
    public Type<ToggleTigerEyeCamouflagePayload> type() {
        return TYPE;
    }
}
