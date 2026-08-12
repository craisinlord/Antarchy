package com.craisinlord.antarchy.mixins.gravity;

import net.minecraft.server.level.ServerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.server.level.ChunkMap$TrackedEntity")
/*
 * Accessor for tracked entity internals used by gravity sync.
 */
public interface TrackedEntityAccessor {
    @Accessor("serverEntity")
    ServerEntity antarchy$getServerEntity();
}
