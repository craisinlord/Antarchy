package com.craisinlord.antarchy.mixins.gravity;

import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerChunkCache.class)
/*
 * Accessor for server chunk source internals used by gravity sync.
 */
public interface ServerChunkCacheAccessor {
    @Accessor("chunkMap")
    ChunkMap antarchy$getChunkMap();
}
