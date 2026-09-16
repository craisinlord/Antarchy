package com.craisinlord.antarchy.mixins.gravity;

import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisUndersideManager;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

@Mixin(ThrowableProjectile.class)
/*
 * Flips thrown projectile gravity for inverted shooters.
 */
public abstract class ThrowableProjectileGravityMixin {
    @Shadow
    protected abstract float getGravity();

    @WrapOperation(method = "tick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/projectile/ThrowableProjectile;setDeltaMovement(DDD)V"))
    private void antarchy$reverseUndersideGravity(ThrowableProjectile projectile, double x, double y, double z,
                                                   Operation<Void> original) {
        if (ThoraxisUndersideManager.isInUnderside(projectile)) {
            // Vanilla has already subtracted getGravity(); add twice to turn it into an upward acceleration.
            y += this.getGravity() * 2.0D;
        }
        original.call(projectile, x, y, z);
    }
}
