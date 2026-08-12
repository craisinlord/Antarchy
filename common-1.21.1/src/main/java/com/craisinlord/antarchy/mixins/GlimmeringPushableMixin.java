package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.AntarchyObjects;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// LivingEntity.isPushable()/isPickable() are full overrides of Entity's versions (they don't call
// super), so this has to target LivingEntity directly, not Entity, or the injected code never runs.
@Mixin(LivingEntity.class)
public abstract class GlimmeringPushableMixin {
    @Inject(method = "isPushable", at = @At("RETURN"), cancellable = true)
    private void antarchy$glimmeringNotPushable(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ()) {
            return;
        }

        LivingEntity self = (LivingEntity) (Object) this;
        Holder<MobEffect> glimmering = AntarchyObjects.GLIMMERING_EFFECT.get();
        if (glimmering != null && self.hasEffect(glimmering)) {
            cir.setReturnValue(false);
        }
    }

    // Projectiles (and entity-picking in general) skip targets that aren't isPickable(), so this
    // is what actually lets arrows/tridents/etc. pass through a glimmering entity instead of
    // colliding with it; the damage-immunity mixin alone only stopped the damage, not the impact.
    @Inject(method = "isPickable", at = @At("RETURN"), cancellable = true)
    private void antarchy$glimmeringNotPickable(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ()) {
            return;
        }

        LivingEntity self = (LivingEntity) (Object) this;
        Holder<MobEffect> glimmering = AntarchyObjects.GLIMMERING_EFFECT.get();
        if (glimmering != null && self.hasEffect(glimmering)) {
            cir.setReturnValue(false);
        }
    }
}
