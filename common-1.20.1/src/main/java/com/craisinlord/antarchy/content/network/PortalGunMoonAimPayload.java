package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import com.craisinlord.anthology.network.ByteBufCodecs;
import com.craisinlord.anthology.network.StreamCodec;
import com.craisinlord.anthology.network.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PortalGunMoonAimPayload(boolean tracked, boolean aiming) implements CustomPacketPayload {
    public static final Type<PortalGunMoonAimPayload> TYPE = new Type<>(new ResourceLocation(Antarchy.MODID, "portal_gun_moon_aim"));
    public static final StreamCodec<ByteBuf, PortalGunMoonAimPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, PortalGunMoonAimPayload::tracked,
            ByteBufCodecs.BOOL, PortalGunMoonAimPayload::aiming,
            PortalGunMoonAimPayload::new
    );

    @Override
    public Type<PortalGunMoonAimPayload> type() {
        return TYPE;
    }
}
