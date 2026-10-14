package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.anthology.network.ByteBufCodecs;
import com.craisinlord.anthology.network.CustomPacketPayload;
import com.craisinlord.anthology.network.StreamCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public record AntarchyGamePayload(BlockPos pos, int action, String value) implements CustomPacketPayload {
    public static final int BASILISK_STATE = 16;
    public static final int ANTMAN_STATE = 17;
    public static final Type<AntarchyGamePayload> TYPE = new Type<>(new ResourceLocation(Antarchy.MODID, "antarchy_game"));
    public static final StreamCodec<ByteBuf, AntarchyGamePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, payload -> payload.pos().asLong(),
            ByteBufCodecs.VAR_INT, AntarchyGamePayload::action,
            ByteBufCodecs.STRING_UTF8, AntarchyGamePayload::value,
            (pos, action, value) -> new AntarchyGamePayload(BlockPos.of(pos), action, value)
    );

    public AntarchyGamePayload(BlockPos pos, int action) {
        this(pos, action, "");
    }

    @Override
    public Type<AntarchyGamePayload> type() {
        return TYPE;
    }
}
