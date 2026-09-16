package com.craisinlord.antarchy.mixins.gravity;

import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisUndersideManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
/*
 * Entity declares applyGravity in 1.21.1. Restrict the reversed acceleration to
 * projectiles that are physically inside the Thoraxis Underside.
 */
public abstract class EntityProjectileGravityMixin {

    @Inject(method = "applyGravity", at = @At("HEAD"), cancellable = true)
    private void antarchy$applyProjectileGravityUpward(CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (!(self instanceof Projectile projectile)) {
            return;
        }

        boolean inverted = AntarchyGravityApi.isGravityInverted(projectile);
        if (!inverted && projectile.getOwner() != null) {
            inverted = AntarchyGravityApi.isGravityInverted(projectile.getOwner());
        }
        if (!inverted && !ThoraxisUndersideManager.isInUnderside(projectile)) {
            return;
        }

        projectile.setDeltaMovement(
                projectile.getDeltaMovement().add(0.0D, projectile.getGravity(), 0.0D)
        );
        ci.cancel();
    }
}
