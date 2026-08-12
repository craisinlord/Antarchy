package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.AntarchyObjects;
import net.minecraft.core.Holder;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class GlimmeringDamageImmunityMixin {
    // Magic damage (potions, spells) is tagged bypasses_armor, so it's excluded
    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void antarchy$glimmeringIgnoresAttacks(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.level().isClientSide()) {
            return;
        }

        if (source.getEntity() == null || source.is(DamageTypeTags.BYPASSES_ARMOR)) {
            return;
        }

        Holder<MobEffect> glimmering = AntarchyObjects.GLIMMERING_EFFECT.get();
        if (glimmering != null && self.hasEffect(glimmering)) {
            cir.setReturnValue(false);
        }
    }
}
