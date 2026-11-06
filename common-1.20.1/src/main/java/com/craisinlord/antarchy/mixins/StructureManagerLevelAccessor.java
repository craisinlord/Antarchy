package com.craisinlord.antarchy.mixins;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(StructureManager.class)
public interface StructureManagerLevelAccessor {
    @Accessor("level")
    LevelAccessor antarchy$level();
}
