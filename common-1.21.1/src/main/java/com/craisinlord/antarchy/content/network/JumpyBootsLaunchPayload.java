package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record JumpyBootsLaunchPayload(int chargeTicks, boolean sprinting) implements CustomPacketPayload {
    public static final Type<JumpyBootsLaunchPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "jumpy_boots_launch"));
    public static final StreamCodec<ByteBuf, JumpyBootsLaunchPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, JumpyBootsLaunchPayload::chargeTicks,
                    ByteBufCodecs.BOOL, JumpyBootsLaunchPayload::sprinting,
                    JumpyBootsLaunchPayload::new
            );

    @Override
    public Type<JumpyBootsLaunchPayload> type() {
        return TYPE;
    }
}
