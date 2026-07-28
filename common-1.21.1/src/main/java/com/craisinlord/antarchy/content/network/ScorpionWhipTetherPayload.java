package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ScorpionWhipTetherPayload(int playerId, int targetId) implements CustomPacketPayload {
    public static final Type<ScorpionWhipTetherPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "scorpion_whip_tether"));

    public static final StreamCodec<ByteBuf, ScorpionWhipTetherPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            ScorpionWhipTetherPayload::playerId,
            ByteBufCodecs.VAR_INT,
            ScorpionWhipTetherPayload::targetId,
            ScorpionWhipTetherPayload::new
    );

    @Override
    public Type<ScorpionWhipTetherPayload> type() {
        return TYPE;
    }
}
