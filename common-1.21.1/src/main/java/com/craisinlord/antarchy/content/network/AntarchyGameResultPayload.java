package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record AntarchyGameResultPayload(BlockPos pos, boolean success, String data) implements CustomPacketPayload {
    public static final Type<AntarchyGameResultPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "antarchy_game_result"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AntarchyGameResultPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, AntarchyGameResultPayload::pos,
            ByteBufCodecs.BOOL, AntarchyGameResultPayload::success,
            ByteBufCodecs.stringUtf8(65536), AntarchyGameResultPayload::data,
            AntarchyGameResultPayload::new
    );

    @Override
    public Type<AntarchyGameResultPayload> type() {
        return TYPE;
    }
}
