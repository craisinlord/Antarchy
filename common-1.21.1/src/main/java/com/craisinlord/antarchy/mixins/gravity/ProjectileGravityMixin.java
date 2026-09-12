package com.craisinlord.antarchy.mixins.gravity;

import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisUndersideManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Projectile.class)
/*
 * Gives every projectile a gravity frame based on its own position in Thoraxis.
 * A shooter can influence the initial aim, but cannot leave a projectile inverted
 * after it exits the Underside.
 */
public abstract class ProjectileGravityMixin {

    @Inject(method = "shootFromRotation", at = @At("HEAD"))
    private void antarchy$prepareGravityAwareShot(
            Entity shooter,
            float xRot,
            float yRot,
            float rotationOffset,
            float velocity,
            float inaccuracy,
            CallbackInfo ci
    ) {
        ThoraxisUndersideManager.updateProjectileGravity((Projectile) (Object) this);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void antarchy$updateUndersideGravity(CallbackInfo ci) {
        ThoraxisUndersideManager.updateProjectileGravity((Projectile) (Object) this);
    }
}
