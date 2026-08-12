package com.craisinlord.antarchy.mixins.gravity;

import net.minecraft.server.level.ServerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerEntity.class)
/*
 * Accessor for server entity tracking internals.
 */
public interface ServerEntityAccessor {
    @Accessor("updateInterval")
    void antarchy$setUpdateInterval(int updateInterval);
}
