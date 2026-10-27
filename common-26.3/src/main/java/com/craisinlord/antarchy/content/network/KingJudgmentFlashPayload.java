package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record KingJudgmentFlashPayload(int durationTicks) implements CustomPacketPayload {
    public static final Type<KingJudgmentFlashPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Antarchy.MODID, "king_judgment_flash"));
    public static final StreamCodec<ByteBuf, KingJudgmentFlashPayload> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(KingJudgmentFlashPayload::new, KingJudgmentFlashPayload::durationTicks);

    @Override
    public Type<KingJudgmentFlashPayload> type() {
        return TYPE;
    }
}
