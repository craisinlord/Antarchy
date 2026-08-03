package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record TigerEyeCamouflageStatePayload(int entityId, boolean active, int blockStateId) implements CustomPacketPayload {
    public static final Type<TigerEyeCamouflageStatePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "tiger_eye_camouflage_state"));
    public static final StreamCodec<ByteBuf, TigerEyeCamouflageStatePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, TigerEyeCamouflageStatePayload::entityId,
            ByteBufCodecs.BOOL, TigerEyeCamouflageStatePayload::active,
            ByteBufCodecs.VAR_INT, TigerEyeCamouflageStatePayload::blockStateId,
            TigerEyeCamouflageStatePayload::new
    );

    @Override
    public Type<TigerEyeCamouflageStatePayload> type() {
        return TYPE;
    }
}
