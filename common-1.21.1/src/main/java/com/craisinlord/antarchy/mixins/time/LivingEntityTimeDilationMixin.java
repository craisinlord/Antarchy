package com.craisinlord.antarchy.mixins.time;

import com.craisinlord.antarchy.content.time.TimeDilationApi;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityTimeDilationMixin {
    @Inject(method = "updateSwingTime", at = @At("HEAD"), cancellable = true)
    private void antarchy$slowSwingTime(CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (!TimeDilationApi.consumeTick(entity, "living_swing")) {
            ci.cancel();
        }
    }

    @Inject(method = "tickEffects", at = @At("HEAD"), cancellable = true)
    private void antarchy$slowEffectTicks(CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (!TimeDilationApi.consumeTick(entity, "living_effects")) {
            ci.cancel();
        }
    }

    @Inject(method = "updatingUsingItem", at = @At("HEAD"), cancellable = true)
    private void antarchy$slowUsingItem(CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (!TimeDilationApi.consumeTick(entity, "living_using_item")) {
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

    @Inject(method = "updateUsingItem", at = @At("HEAD"), cancellable = true)
    private void antarchy$slowItemUseProgress(ItemStack stack, CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (!TimeDilationApi.consumeTick(entity, "living_item_use_progress")) {
            ci.cancel();
        }
    }
}
