package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record WormHookTetherPayload(int playerId, int hookId) implements CustomPacketPayload {
    public static final Type<WormHookTetherPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "worm_hook_tether"));

    public static final StreamCodec<ByteBuf, WormHookTetherPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            WormHookTetherPayload::playerId,
            ByteBufCodecs.VAR_INT,
            WormHookTetherPayload::hookId,
            WormHookTetherPayload::new
    );

    @Override
    public Type<WormHookTetherPayload> type() {
        return TYPE;
    }
}
