package com.craisinlord.antarchy.fabric.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public abstract class ElytraFallFlyingMixin {

    /**
     * Redirect to allow any ElytraItem subclass to sustain fall flying.
     */
    @Redirect(
            method = "updateFallFlying",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"
            )
    )
    private boolean antarchy$allowCustomElytraFallFlying(ItemStack itemStack, Item item) {
        return itemStack.getItem() instanceof ElytraItem;
    }
}
