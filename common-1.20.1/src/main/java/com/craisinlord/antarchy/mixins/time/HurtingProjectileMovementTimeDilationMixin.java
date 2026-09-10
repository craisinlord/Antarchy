package com.craisinlord.antarchy.mixins.time;

import com.craisinlord.antarchy.content.time.TimeDilationApi;
import com.craisinlord.antarchy.content.time.TimeDilationMath;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractHurtingProjectile.class)
public abstract class HurtingProjectileMovementTimeDilationMixin {
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/AbstractHurtingProjectile;setPos(DDD)V"))
    private void antarchy$slowPosition(AbstractHurtingProjectile projectile, double x, double y, double z) {
        double rate = Math.max(TimeDilationMath.MIN_RATE, TimeDilationApi.getRate(projectile));
        if (rate >= TimeDilationMath.NORMAL_RATE) {
            projectile.setPos(x, y, z);
            return;
        }
        projectile.setPos(projectile.getX() + (x - projectile.getX()) * rate,
                projectile.getY() + (y - projectile.getY()) * rate,
                projectile.getZ() + (z - projectile.getZ()) * rate);
    }
}
