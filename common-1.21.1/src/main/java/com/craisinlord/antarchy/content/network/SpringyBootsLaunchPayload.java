package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SpringyBootsLaunchPayload(int chargeTicks, boolean sprinting) implements CustomPacketPayload {
    public static final Type<SpringyBootsLaunchPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "springy_boots_launch"));
    public static final StreamCodec<ByteBuf, SpringyBootsLaunchPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, SpringyBootsLaunchPayload::chargeTicks,
                    ByteBufCodecs.BOOL, SpringyBootsLaunchPayload::sprinting,
                    SpringyBootsLaunchPayload::new
            );

    @Override
    public Type<SpringyBootsLaunchPayload> type() {
        return TYPE;
    }
}
