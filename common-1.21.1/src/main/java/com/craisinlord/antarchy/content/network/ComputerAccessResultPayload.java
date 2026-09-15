package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ComputerAccessResultPayload(BlockPos pos, int result, boolean hasPassword, boolean authenticated, String data) implements CustomPacketPayload {
    public static final int SETUP_REQUIRED = 0;
    public static final int READY = 1;
    public static final int SUCCESS = 2;
    public static final int INVALID_PASSWORD = 3;
    public static final int BUSY = 4;
    public static final int INVALID = 5;
    public static final Type<ComputerAccessResultPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "computer_access_result"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ComputerAccessResultPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ComputerAccessResultPayload::pos,
            ByteBufCodecs.VAR_INT, ComputerAccessResultPayload::result,
            ByteBufCodecs.BOOL, ComputerAccessResultPayload::hasPassword,
            ByteBufCodecs.BOOL, ComputerAccessResultPayload::authenticated,
            ByteBufCodecs.stringUtf8(65536), ComputerAccessResultPayload::data,
            ComputerAccessResultPayload::new
    );

    public ComputerAccessResultPayload(BlockPos pos, int result, boolean hasPassword, boolean authenticated) {
        this(pos, result, hasPassword, authenticated, "");
    }

    @Override
    public Type<ComputerAccessResultPayload> type() {
        return TYPE;
    }
}
