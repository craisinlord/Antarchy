package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.effect.CommandedBehavior;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class MobCommandedTargetMixin {
    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void antarchy$protectCommandedAllies(LivingEntity target, CallbackInfo ci) {
        Mob mob = (Mob) (Object) this;
        if (target != null && CommandedBehavior.isProtectedAlly(mob, target)) {
            ci.cancel();
        }
    }

    @Inject(method = "doHurtTarget", at = @At("HEAD"), cancellable = true)
    private void antarchy$preventCommandedFriendlyFire(net.minecraft.world.entity.Entity target,
            org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir) {
        Mob mob = (Mob) (Object) this;
        if (CommandedBehavior.isProtectedAlly(mob, target)) {
            cir.setReturnValue(false);
        }
    }
}
