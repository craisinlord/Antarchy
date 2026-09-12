package com.craisinlord.antarchy.mixins.gravity;

import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisUndersideManager;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ThrowableProjectile.class)
/*
 * Flips thrown projectile gravity for inverted shooters.
 */
public abstract class ThrowableProjectileGravityMixin {

    @WrapOperation(method = "tick", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/projectile/ThrowableProjectile;applyGravity()V"))
    private void antarchy$applyThrownProjectileGravityUpward(ThrowableProjectile self, Operation<Void> original) {
        Entity owner = self.getOwner();
        if (!(owner instanceof LivingEntity living)) {
            original.call(self);
            return;
        }
        if (owner instanceof Player && !AntarchySettings.invertProjectilesFromInvertedPlayers()) {
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
