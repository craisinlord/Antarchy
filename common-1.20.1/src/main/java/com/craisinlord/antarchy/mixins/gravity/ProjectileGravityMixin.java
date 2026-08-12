package com.craisinlord.antarchy.mixins.gravity;

import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Projectile.class)
/*
 * Flips generic projectile gravity for inverted shooters.
 */
public abstract class ProjectileGravityMixin {
}
