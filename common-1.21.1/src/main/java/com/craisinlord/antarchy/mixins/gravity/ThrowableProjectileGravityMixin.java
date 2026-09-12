package com.craisinlord.antarchy.mixins.gravity;

import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisUndersideManager;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ThrowableProjectile.class)
/*
 * Flips thrown projectile gravity from the projectile's current position.
 */
public abstract class ThrowableProjectileGravityMixin {

    @WrapOperation(method = "tick", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/projectile/ThrowableProjectile;applyGravity()V"))
    private void antarchy$applyThrownProjectileGravityUpward(ThrowableProjectile self, Operation<Void> original) {
        if (!ThoraxisUndersideManager.isInUnderside(self)) {
            original.call(self);
            return;
        }
        // Vanilla subtracts gravity from world Y; the Underside adds it instead.
        double gravity = self.getGravity();
        self.setDeltaMovement(self.getDeltaMovement().add(0.0D, gravity, 0.0D));
    }
}
