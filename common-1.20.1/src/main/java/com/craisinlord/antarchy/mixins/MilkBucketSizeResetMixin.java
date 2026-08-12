package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.entity.SizeRayProjectileEntity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class MilkBucketSizeResetMixin {
    @Inject(method = "removeAllEffects", at = @At("TAIL"))
    private void antarchy$resetSizeRayScale(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.level().isClientSide) {
            return;
        }

        SizeRayProjectileEntity.resetScale(self);
    }
}
