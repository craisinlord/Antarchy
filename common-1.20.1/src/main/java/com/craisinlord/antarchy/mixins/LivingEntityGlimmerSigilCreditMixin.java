package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.glimmerecho.GlimmerEchoes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityGlimmerSigilCreditMixin {
    @Inject(method = "die", at = @At("HEAD"))
    private void antarchy$shareSigilBossKill(DamageSource source, CallbackInfo ci) {
        GlimmerEchoes.shareSigilBossKill((LivingEntity) (Object) this, source);
    }
}
