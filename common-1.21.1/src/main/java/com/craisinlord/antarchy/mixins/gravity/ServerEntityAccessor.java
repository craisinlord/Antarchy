package com.craisinlord.antarchy.mixins.gravity;

import net.minecraft.network.protocol.game.VecDeltaCodec;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerEntity.class)
/*
 * Accessor for server entity tracking internals.
 */
public interface ServerEntityAccessor {
    @Accessor("positionCodec")
    VecDeltaCodec antarchy$getPositionCodec();

    @Accessor("lastSentMovement")
    void antarchy$setLastSentMovement(Vec3 lastSentMovement);

    @Accessor("updateInterval")
    void antarchy$setUpdateInterval(int updateInterval);
}
