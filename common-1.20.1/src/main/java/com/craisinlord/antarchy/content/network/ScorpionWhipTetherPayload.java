package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.anthology.network.ByteBufCodecs;
import com.craisinlord.anthology.network.CustomPacketPayload;
import com.craisinlord.anthology.network.StreamCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.resources.ResourceLocation;

public record ScorpionWhipTetherPayload(int playerId, int targetId) implements CustomPacketPayload {
    public static final Type<ScorpionWhipTetherPayload> TYPE =
            new Type<>(new ResourceLocation(Antarchy.MODID, "scorpion_whip_tether"));

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
