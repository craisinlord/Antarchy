package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.effect.GoopedEffectHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class GoopedStepSoundMixin {
    @Inject(method = "playStepSound", at = @At("HEAD"), cancellable = true)
    private void antarchy$goopedStepSound(BlockPos pos, BlockState state, CallbackInfo ci) {
        if (!((Object) this instanceof LivingEntity livingEntity)) {
            return;
        }

        Holder<MobEffect> gooped = GoopedEffectHooks.holder();
        if (gooped == null || !livingEntity.hasEffect(gooped.value())) {
            return;
        }

        livingEntity.playSound(SoundEvents.SLIME_BLOCK_STEP, 0.15F, 1.0F);
        ci.cancel();
    }
}
