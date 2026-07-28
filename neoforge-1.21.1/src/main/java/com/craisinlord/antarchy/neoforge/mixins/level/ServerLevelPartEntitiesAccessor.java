package com.craisinlord.antarchy.neoforge.mixins.level;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.entity.PartEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerLevel.class)
/*
 * Accessor for server part-entity storage.
 */
public interface ServerLevelPartEntitiesAccessor {
    @Accessor("dragonParts")
    Int2ObjectMap<PartEntity<?>> antarchy$getPartEntities();
}
