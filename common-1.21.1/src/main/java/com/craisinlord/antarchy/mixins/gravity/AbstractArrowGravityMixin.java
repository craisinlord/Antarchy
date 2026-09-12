package com.craisinlord.antarchy.mixins.gravity;

import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisUndersideManager;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractArrow.class)
/*
 * Flips arrow gravity for inverted shooters.
 */
public abstract class AbstractArrowGravityMixin {

    @WrapOperation(method = "tick", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;applyGravity()V"))
    private void antarchy$applyArrowGravityUpward(AbstractArrow self, Operation<Void> original) {
        Entity owner = self.getOwner();
        if (!(owner instanceof LivingEntity living)) {
            original.call(self);
            return;
        }
        if (owner instanceof net.minecraft.world.entity.player.Player && !AntarchySettings.invertProjectilesFromInvertedPlayers()) {
            original.call(self);
            return;
        }
        if (!ThoraxisUndersideManager.shouldInvertInUnderside(living)) {
            original.call(self);
            return;
        }
        // Vanilla applies gravity as -getGravity(). Negate that acceleration to
        // mirror the projectile's arc for the inverted Underside gravity.
        double gravity = self.getGravity();
        self.setDeltaMovement(self.getDeltaMovement().add(0.0D, -gravity, 0.0D));
    }
}
