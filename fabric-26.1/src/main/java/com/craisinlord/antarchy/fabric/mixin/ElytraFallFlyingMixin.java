package com.craisinlord.antarchy.fabric.mixin;

import com.craisinlord.antarchy.content.item.BrutalflyElytraItem;
import com.craisinlord.antarchy.content.item.ManticoreWingsItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class ElytraFallFlyingMixin {

    @Inject(method = "canGlideUsing", at = @At("RETURN"), cancellable = true)
    private static void antarchy$allowCustomElytraFallFlying(ItemStack itemStack, EquipmentSlot slot, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            return;
        }
        if (slot == EquipmentSlot.CHEST && antarchy$isCustomElytra(itemStack) && !itemStack.nextDamageWillBreak()) {
            cir.setReturnValue(true);
        }
    }

    private static boolean antarchy$isCustomElytra(ItemStack itemStack) {
        return itemStack.getItem() instanceof BrutalflyElytraItem || itemStack.getItem() instanceof ManticoreWingsItem;
    }
}
