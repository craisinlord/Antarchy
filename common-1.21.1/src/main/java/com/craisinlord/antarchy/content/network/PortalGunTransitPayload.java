package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public record PortalGunTransitPayload(int portalId, Vec3 start, Vec3 end, Vec3 velocity) implements CustomPacketPayload {
    public static final Type<PortalGunTransitPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "portal_gun_transit"));
    public static final StreamCodec<ByteBuf, PortalGunTransitPayload> STREAM_CODEC = StreamCodec.of(PortalGunTransitPayload::write, PortalGunTransitPayload::read);

    private static void write(ByteBuf buffer, PortalGunTransitPayload payload) {
        buffer.writeInt(payload.portalId());
        writeVec(buffer, payload.start());
        writeVec(buffer, payload.end());
        writeVec(buffer, payload.velocity());
    }

    private static PortalGunTransitPayload read(ByteBuf buffer) {
        int portalId = buffer.readInt();
        Vec3 start = readVec(buffer);
        Vec3 end = readVec(buffer);
        Vec3 velocity = readVec(buffer);
        return new PortalGunTransitPayload(portalId, start, end, velocity);
    }

    private static void writeVec(ByteBuf buffer, Vec3 vector) {
        buffer.writeDouble(vector.x);
        buffer.writeDouble(vector.y);
        buffer.writeDouble(vector.z);
    }

    private static Vec3 readVec(ByteBuf buffer) {
        return new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }

    @Override
    public Type<PortalGunTransitPayload> type() {
        return TYPE;
    }
}
