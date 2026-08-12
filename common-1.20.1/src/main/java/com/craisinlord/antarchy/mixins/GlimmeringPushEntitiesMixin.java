package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.AntarchyObjects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class GlimmeringPushEntitiesMixin {
    @Inject(method = "pushEntities", at = @At("HEAD"), cancellable = true)
    private void antarchy$glimmeringSkipsPushingOthers(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        MobEffect glimmering = AntarchyObjects.GLIMMERING_EFFECT.get();
        if (glimmering != null && self.hasEffect(glimmering)) {
            ci.cancel();
        }
    }
}
