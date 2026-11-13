package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.anthology.network.ByteBufCodecs;
import com.craisinlord.anthology.network.CustomPacketPayload;
import com.craisinlord.anthology.network.StreamCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.resources.ResourceLocation;

public record KingJudgmentFlashPayload(int durationTicks) implements CustomPacketPayload {
    public static final Type<KingJudgmentFlashPayload> TYPE =
            new Type<>(new ResourceLocation(Antarchy.MODID, "king_judgment_flash"));
    public static final StreamCodec<ByteBuf, KingJudgmentFlashPayload> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(KingJudgmentFlashPayload::new, KingJudgmentFlashPayload::durationTicks);

    @Override
    public Type<KingJudgmentFlashPayload> type() {
        return TYPE;
    }
}
