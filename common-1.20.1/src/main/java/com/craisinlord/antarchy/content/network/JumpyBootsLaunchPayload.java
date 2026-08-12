package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import com.craisinlord.antarchy.compat.network.ByteBufCodecs;
import com.craisinlord.antarchy.compat.network.StreamCodec;
import com.craisinlord.antarchy.compat.network.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record JumpyBootsLaunchPayload(int chargeTicks, boolean sprinting) implements CustomPacketPayload {
    public static final Type<JumpyBootsLaunchPayload> TYPE =
            new Type<>(new ResourceLocation(Antarchy.MODID, "jumpy_boots_launch"));
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
