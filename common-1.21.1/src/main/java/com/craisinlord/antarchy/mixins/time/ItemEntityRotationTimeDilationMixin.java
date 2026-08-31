package com.craisinlord.antarchy.mixins.time;

import com.craisinlord.antarchy.content.client.ClientTimeDilationTicker;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;

@Mixin(ItemEntity.class)
public abstract class ItemEntityRotationTimeDilationMixin {
    @ModifyReturnValue(method = "getSpin", at = @At("RETURN"))
    private float antarchy$slowSpin(float spin) {
        return ClientTimeDilationTicker.dilateItemSpin((ItemEntity) (Object) this, spin);
    }
}
