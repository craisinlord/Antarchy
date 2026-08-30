package com.craisinlord.antarchy.mixins.time;

import com.craisinlord.antarchy.content.time.TimeDilationApi;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.item.PrimedTnt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PrimedTnt.class)
public abstract class PrimedTntTimeDilationMixin {
    @WrapOperation(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/PrimedTnt;setFuse(I)V")
    )
    private void antarchy$slowFuse(PrimedTnt tnt, int fuse, Operation<Void> original) {
        if (TimeDilationApi.consumeTick(tnt, "primed_tnt_fuse")) {
            original.call(tnt, fuse);
        }
    }
}
