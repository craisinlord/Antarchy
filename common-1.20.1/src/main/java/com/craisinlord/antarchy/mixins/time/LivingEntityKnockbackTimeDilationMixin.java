package com.craisinlord.antarchy.mixins.time;

import com.craisinlord.antarchy.content.time.TimeDilationEntityAccess;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityKnockbackTimeDilationMixin {
    @Inject(method = "knockback", at = @At("HEAD"))
    private void antarchy$beginExternalImpulse(double strength, double x, double z, CallbackInfo ci) {
        if (this instanceof TimeDilationEntityAccess access) {
            access.antarchy$setApplyingExternalImpulse(true);
        }
    }

    @Inject(method = "knockback", at = @At("RETURN"))
    private void antarchy$endExternalImpulse(double strength, double x, double z, CallbackInfo ci) {
        if (this instanceof TimeDilationEntityAccess access) {
            access.antarchy$setApplyingExternalImpulse(false);
        }
    }
}
