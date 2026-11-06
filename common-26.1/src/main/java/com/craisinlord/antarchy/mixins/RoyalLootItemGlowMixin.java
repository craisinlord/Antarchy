package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.entity.royal.RoyalLootDropGlow;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class RoyalLootItemGlowMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void antarchy$tickRoyalLootGlow(CallbackInfo ci) {
        RoyalLootDropGlow.tick((ItemEntity) (Object) this);
    }
}
