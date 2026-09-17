package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.effect.RoyalBoundaryManager;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Projectile.class)
public abstract class ProjectileRoyalBoundaryMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void antarchy$prepareRoyalBoundary(CallbackInfo ci) {
        RoyalBoundaryManager.prepareProjectile((Projectile) (Object) this);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void antarchy$processRoyalBoundary(CallbackInfo ci) {
        RoyalBoundaryManager.tickProjectile((Projectile) (Object) this);
    }
}
