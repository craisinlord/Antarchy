package com.craisinlord.antarchy.mixins.time;

import com.craisinlord.antarchy.content.time.TimeDilationApi;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityTimeDilationCleanupMixin {
    @Inject(method = "die", at = @At("HEAD"))
    private void antarchy$clearTimeDilationOnDeath(DamageSource damageSource, CallbackInfo ci) {
        TimeDilationApi.clearRate((LivingEntity) (Object) this);
    }
}
