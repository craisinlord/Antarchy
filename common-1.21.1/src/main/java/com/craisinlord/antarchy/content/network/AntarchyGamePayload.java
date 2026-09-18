package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record AntarchyGamePayload(BlockPos pos, int action, String value) implements CustomPacketPayload {
    public static final int BASILISK_STATE = 16;
    public static final int ANTMAN_STATE = 17;
    public static final int BLOCKLE_STATE = 18;
    public static final int BLOCKLE_GUESS = 19;
    public static final Type<AntarchyGamePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "antarchy_game"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AntarchyGamePayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, AntarchyGamePayload::pos,
            ByteBufCodecs.VAR_INT, AntarchyGamePayload::action,
            ByteBufCodecs.stringUtf8(65536), AntarchyGamePayload::value,
            AntarchyGamePayload::new
    );

    public AntarchyGamePayload(BlockPos pos, int action) {
        this(pos, action, "");
    }

    @Override
    public Type<AntarchyGamePayload> type() {
        return TYPE;
    }
}
