package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.AntarchyObjects;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class AmericanFoodNameMixin {
    @Inject(method = "getHoverName", at = @At("RETURN"), cancellable = true)
    private void antarchy$prefixAmericanName(CallbackInfoReturnable<Component> cir) {
        ItemStack self = (ItemStack) (Object) this;
        if (self.has(AntarchyObjects.AMERICAN_COMPONENT.get())) {
            cir.setReturnValue(Component.translatable("item.antarchy.american_name", cir.getReturnValue()));
        }
    }
}
