package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import com.craisinlord.anthology.network.ByteBufCodecs;
import com.craisinlord.anthology.network.StreamCodec;
import com.craisinlord.anthology.network.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SpringyBootsLaunchPayload(int chargeTicks, boolean sprinting) implements CustomPacketPayload {
    public static final Type<SpringyBootsLaunchPayload> TYPE =
            new Type<>(new ResourceLocation(Antarchy.MODID, "springy_boots_launch"));
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
