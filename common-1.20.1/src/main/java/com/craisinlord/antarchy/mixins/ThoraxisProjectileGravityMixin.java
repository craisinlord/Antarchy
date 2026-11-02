package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.weather.ThoraxisEventMath;
import com.craisinlord.anthology.gravity.GravityHooks;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ThrowableProjectile.class)
public abstract class ThoraxisProjectileGravityMixin {
    @ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/projectile/ThrowableProjectile;getGravity()F"))
    private float antarchy$thoraxisGravity(float gravity) {
        ThrowableProjectile self = (ThrowableProjectile) (Object) this;
        if (GravityHooks.shouldInvertProjectileGravity(self)) {
            return gravity;
        }
        return (float) ThoraxisEventMath.scaleGravity(self, gravity);
    }
}
