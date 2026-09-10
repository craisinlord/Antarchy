package com.craisinlord.antarchy.mixins.time;

import com.craisinlord.antarchy.content.time.TimeDilationApi;
import com.craisinlord.antarchy.content.time.TimeDilationManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Projectile.class)
public abstract class ProjectileTimeDilationMixin {
    @Inject(method = "setOwner", at = @At("TAIL"))
    private void antarchy$inheritTimeDilation(Entity owner, CallbackInfo callbackInfo) {
        if (owner != null) {
            TimeDilationApi.setInheritedRate((Entity) (Object) this, TimeDilationApi.getRate(owner));
            TimeDilationManager.trackPotentiallyAffected((Entity) (Object) this);
        }
    }
}
