package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ComputerAccessPayload(BlockPos pos, int action, String value) implements CustomPacketPayload {
    public static final int OPEN = 0;
    public static final int SETUP = 1;
    public static final int LOGIN = 2;
    public static final int LOGOUT = 3;
    public static final int CHANGE_PASSWORD = 4;
    public static final int EJECT = 5;
    public static final int CLOSE = 6;
    public static final int FILE_LIST = 7;
    public static final int FILE_OPEN = 8;
    public static final int FILE_CREATE = 9;
    public static final int FILE_SAVE = 10;
    public static final int DESKTOP_STATE = 11;
    public static final int DESKTOP_WALLPAPER = 12;
    public static final int FILE_DELETE = 13;
    public static final int FILE_MOVE = 14;
    public static final int TERMINAL_COMMAND = 15;
    public static final int BASILISK_STATE = 16;
    public static final int ANTMAN_STATE = 17;
    public static final int BLOCKLE_STATE = 18;
    public static final int BLOCKLE_GUESS = 19;
    public static final Type<ComputerAccessPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "computer_access"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ComputerAccessPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ComputerAccessPayload::pos,
            ByteBufCodecs.VAR_INT, ComputerAccessPayload::action,
            ByteBufCodecs.stringUtf8(65536), ComputerAccessPayload::value,
            ComputerAccessPayload::new
    );

    public ComputerAccessPayload(BlockPos pos, int action) {
        this(pos, action, "");
    }

    @Override
    public Type<ComputerAccessPayload> type() {
        return TYPE;
    }
}
