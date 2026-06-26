package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.item.SquidzookaItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public abstract class SquidzookaEnchantMixin {
    private static final Registry<Enchantment> ENCHANTMENT_REGISTRY = resolveEnchantmentRegistry();

    @Inject(method = "canEnchant", at = @At("HEAD"), cancellable = true)
    private void antarchy$limitSquidzookaCrossbowEnchants(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        this.antarchy$applySquidzookaEnchantRules(stack, cir);
    }

    @Inject(method = "isPrimaryItem", at = @At("HEAD"), cancellable = true)
    private void antarchy$limitSquidzookaPrimaryItem(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        this.antarchy$applySquidzookaEnchantRules(stack, cir);
    }

    @Inject(method = "isSupportedItem", at = @At("HEAD"), cancellable = true)
    private void antarchy$limitSquidzookaSupportedItem(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        this.antarchy$applySquidzookaEnchantRules(stack, cir);
    }

    private void antarchy$applySquidzookaEnchantRules(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!(stack.getItem() instanceof SquidzookaItem)) {
            return;
        }

        if (ENCHANTMENT_REGISTRY == null) {
            return;
        }

        Enchantment enchantment = (Enchantment) (Object) this;
        ResourceLocation enchantmentId = ENCHANTMENT_REGISTRY.getKey(enchantment);
        if (enchantmentId == null) {
            return;
        }

        if (enchantmentId.equals(Enchantments.MULTISHOT.location())) {
            cir.setReturnValue(true);
            return;
        }

        if (enchantmentId.equals(Enchantments.QUICK_CHARGE.location())
                || enchantmentId.equals(Enchantments.PIERCING.location())) {
            cir.setReturnValue(false);
        }
    }

    @SuppressWarnings("unchecked")
    private static Registry<Enchantment> resolveEnchantmentRegistry() {
        try {
            return (Registry<Enchantment>) BuiltInRegistries.class.getField("ENCHANTMENT").get(null);
        } catch (ReflectiveOperationException primary) {
            try {
                return (Registry<Enchantment>) BuiltInRegistries.class.getField("ENCHANTMENTS").get(null);
            } catch (ReflectiveOperationException ignored) {
                return null;
            }
        }
    }
}
