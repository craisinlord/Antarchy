package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.anthology.network.ByteBufCodecs;
import com.craisinlord.anthology.network.CustomPacketPayload;
import com.craisinlord.anthology.network.StreamCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.resources.ResourceLocation;

public record WormHookTetherPayload(int playerId, int hookId) implements CustomPacketPayload {
    public static final Type<WormHookTetherPayload> TYPE =
            new Type<>(new ResourceLocation(Antarchy.MODID, "worm_hook_tether"));

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
