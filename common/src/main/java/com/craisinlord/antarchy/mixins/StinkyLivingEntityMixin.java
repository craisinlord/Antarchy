package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.StinkyBehavior;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class StinkyLivingEntityMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void antarchy$tickStinkyTrail(CallbackInfo ci) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        if (livingEntity.hasEffect(AntarchyObjects.STINKY_EFFECT.get())) {
            StinkyBehavior.tickStinkyTrail(livingEntity);
        }
    }
}
