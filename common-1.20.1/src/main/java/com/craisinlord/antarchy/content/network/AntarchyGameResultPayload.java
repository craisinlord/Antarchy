package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.anthology.network.ByteBufCodecs;
import com.craisinlord.anthology.network.CustomPacketPayload;
import com.craisinlord.anthology.network.StreamCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public record AntarchyGameResultPayload(BlockPos pos, boolean success, String data) implements CustomPacketPayload {
    public static final Type<AntarchyGameResultPayload> TYPE = new Type<>(new ResourceLocation(Antarchy.MODID, "antarchy_game_result"));
    public static final StreamCodec<ByteBuf, AntarchyGameResultPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, payload -> payload.pos().asLong(),
            ByteBufCodecs.BOOL, AntarchyGameResultPayload::success,
            ByteBufCodecs.STRING_UTF8, AntarchyGameResultPayload::data,
            (pos, success, data) -> new AntarchyGameResultPayload(BlockPos.of(pos), success, data)
    );

    @Override
    public Type<AntarchyGameResultPayload> type() {
        return TYPE;
    }
}
