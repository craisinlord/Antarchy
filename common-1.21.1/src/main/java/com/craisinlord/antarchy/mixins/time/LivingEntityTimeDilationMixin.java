package com.craisinlord.antarchy.mixins.time;

import com.craisinlord.antarchy.content.time.TimeDilationApi;
import com.craisinlord.antarchy.content.time.TimeDilationManager;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityTimeDilationMixin {
    @Inject(method = "tickEffects", at = @At("HEAD"))
    private void antarchy$trackTemporalEffects(CallbackInfo ci) {
        TimeDilationManager.trackPotentiallyAffected((Entity) (Object) this);
    }

    @Inject(method = "updateSwingTime", at = @At("HEAD"), cancellable = true)
    private void antarchy$slowSwingTime(CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (!TimeDilationApi.consumeTick(entity, "living_swing")) {
            ci.cancel();
        }
    }

    @Inject(method = "swing(Lnet/minecraft/world/InteractionHand;Z)V", at = @At("HEAD"), cancellable = true)
    private void antarchy$slowSwingStart(InteractionHand hand, boolean updateSelf, CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (!TimeDilationApi.consumeTick(entity, "living_swing_start")) {
            ci.cancel();
        }
    }

}
