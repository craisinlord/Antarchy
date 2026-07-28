package com.craisinlord.antarchy.forge.mixins.level;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.entity.PartEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientLevel.class)
/*
 * Accessor for client part-entity storage.
 */
public interface ClientLevelPartEntitiesAccessor {
    @Accessor("partEntities")
    Int2ObjectMap<PartEntity<?>> antarchy$getPartEntities();
}
