package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityDirection;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record GravityStatePayload(
        int entityId,
        int directionId,
        int previousDirectionId,
        boolean forced,
        int transitionDuration,
        int transitionRemaining
) implements CustomPacketPayload {
    public static final Type<GravityStatePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "gravity_state"));
    public static final StreamCodec<ByteBuf, GravityStatePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            GravityStatePayload::entityId,
            ByteBufCodecs.VAR_INT,
            GravityStatePayload::directionId,
            ByteBufCodecs.VAR_INT,
            GravityStatePayload::previousDirectionId,
            ByteBufCodecs.BOOL,
            GravityStatePayload::forced,
            ByteBufCodecs.VAR_INT,
            GravityStatePayload::transitionDuration,
            ByteBufCodecs.VAR_INT,
            GravityStatePayload::transitionRemaining,
            GravityStatePayload::new
    );

    public GravityStatePayload(int entityId, AntarchyGravityDirection direction, AntarchyGravityDirection previousDirection, boolean forced, int transitionDuration, int transitionRemaining) {
        this(entityId, direction.getId(), previousDirection.getId(), forced, transitionDuration, transitionRemaining);
    }

    public AntarchyGravityDirection direction() {
        return AntarchyGravityDirection.byId(this.directionId);
    }

    public AntarchyGravityDirection previousDirection() {
        return AntarchyGravityDirection.byId(this.previousDirectionId);
    }

    @Override
    public Type<GravityStatePayload> type() {
        return TYPE;
    }
}
