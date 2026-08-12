package com.craisinlord.antarchy.mixins.gravity;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.Path;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PathNavigation.class)
/*
 * Accessor for path nav internals used by gravity fixes.
 */
public interface PathNavigationAccessor {
    @Accessor("mob")
    Mob antarchy$getMob();

    @Accessor("path")
    Path antarchy$getPath();
}
