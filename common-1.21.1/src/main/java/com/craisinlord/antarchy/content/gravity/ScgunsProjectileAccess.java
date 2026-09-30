package com.craisinlord.antarchy.content.gravity;

import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/** Exposes Scorched Guns' base ProjectileEntity fields to mixins targeting its subclasses. */
public interface ScgunsProjectileAccess {
    @Nullable LivingEntity antarchy$getScgunsShooter();
    double antarchy$getScgunsModifiedGravity();
}
