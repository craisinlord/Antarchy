package com.craisinlord.antarchy.mixins.compat.scguns;

import com.craisinlord.antarchy.content.gravity.ScgunsProjectileAccess;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

/*
 * Shadows must live on the class that declares the field, so the subclasses
 * reach these through ScgunsProjectileAccess instead.
 */
@Mixin(targets = "top.ribs.scguns.entity.projectile.ProjectileEntity")
@Pseudo
public abstract class ScgunsProjectileEntityAccessMixin implements ScgunsProjectileAccess {
    @Shadow(remap = false) @Nullable protected LivingEntity shooter;
    @Shadow(remap = false) protected double modifiedGravity;

    @Override
    public @Nullable LivingEntity antarchy$getScgunsShooter() {
        return this.shooter;
    }

    @Override
    public double antarchy$getScgunsModifiedGravity() {
        return this.modifiedGravity;
    }
}
