package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.entity.royal.KingEntity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class RoyalNoRespiteMixin {
    @Inject(method = "heal", at = @At("HEAD"), cancellable = true)
    private void antarchy$blockRoyalNoRespiteHealing(float amount, CallbackInfo ci) {
        if (KingEntity.blocksHealingAround((LivingEntity) (Object) this)) {
            ci.cancel();
        }
    }
}
