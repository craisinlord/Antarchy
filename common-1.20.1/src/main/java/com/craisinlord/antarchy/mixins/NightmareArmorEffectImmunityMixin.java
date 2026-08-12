package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.item.NightmareArmorItem;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class NightmareArmorEffectImmunityMixin {
    @Inject(method = "canBeAffected", at = @At("HEAD"), cancellable = true)
    private void antarchy$blockNightmareArmorEffects(MobEffectInstance effectInstance, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        if (NightmareArmorItem.isWearingFullSet(livingEntity)
                && (effectInstance.getEffect() == MobEffects.WITHER || effectInstance.getEffect() == AntarchyObjects.DREAD.get())) {
            cir.setReturnValue(false);
        }
    }
}
