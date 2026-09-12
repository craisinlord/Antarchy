package com.craisinlord.antarchy.mixins.gravity;

import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisUndersideManager;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractArrow.class)
/*
 * Flips arrow gravity from the arrow's current position.
 */
public abstract class AbstractArrowGravityMixin {

    @WrapOperation(method = "tick", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;applyGravity()V"))
    private void antarchy$applyArrowGravityUpward(AbstractArrow self, Operation<Void> original) {
        if (!ThoraxisUndersideManager.isInUnderside(self)) {
            original.call(self);
            return;
        }
        // Vanilla subtracts gravity from world Y; the Underside adds it instead.
        double gravity = self.getGravity();
        self.setDeltaMovement(self.getDeltaMovement().add(0.0D, gravity, 0.0D));
    }
}
