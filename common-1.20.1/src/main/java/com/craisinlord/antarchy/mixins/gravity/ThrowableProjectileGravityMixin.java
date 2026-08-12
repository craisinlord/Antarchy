package com.craisinlord.antarchy.mixins.gravity;

import net.minecraft.world.entity.projectile.ThrowableProjectile;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ThrowableProjectile.class)
/*
 * Flips thrown projectile gravity for inverted shooters.
 */
public abstract class ThrowableProjectileGravityMixin {
}
