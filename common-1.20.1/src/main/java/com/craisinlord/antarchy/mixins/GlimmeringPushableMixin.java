package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.AntarchyObjects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class GlimmeringPushableMixin {
    @Inject(method = "isPushable", at = @At("RETURN"), cancellable = true)
    private void antarchy$glimmeringNotPushable(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ()) {
            return;
        }

        LivingEntity self = (LivingEntity) (Object) this;
        MobEffect glimmering = AntarchyObjects.GLIMMERING_EFFECT.get();
        if (glimmering != null && self.hasEffect(glimmering)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isPickable", at = @At("RETURN"), cancellable = true)
    private void antarchy$glimmeringNotPickable(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ()) {
            return;
        }

        LivingEntity self = (LivingEntity) (Object) this;
        MobEffect glimmering = AntarchyObjects.GLIMMERING_EFFECT.get();
        if (glimmering != null && self.hasEffect(glimmering)) {
            cir.setReturnValue(false);
        }
    }
}
