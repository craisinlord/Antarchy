package com.craisinlord.antarchy.mixins.gravity;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.server.level.ChunkMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkMap.class)
/*
 * Accessor for chunk map tracking internals used by gravity sync.
 */
public interface ChunkMapAccessor {
    @Accessor("entityMap")
    Int2ObjectMap<?> antarchy$getEntityMap();
}
