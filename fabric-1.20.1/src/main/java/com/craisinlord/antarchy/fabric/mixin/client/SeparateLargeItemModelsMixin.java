package com.craisinlord.antarchy.fabric.mixin.client;

import com.craisinlord.antarchy.fabric.client.SeparateLargeItemModels;
import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.fabric.api.client.model.loading.v1.FabricBakedModelManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemRenderer.class)
public abstract class SeparateLargeItemModelsMixin {
    @ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private BakedModel antarchy$selectGuiModel(BakedModel original,
                                               @Local(argsOnly = true) ItemStack stack,
                                               @Local(argsOnly = true) ItemDisplayContext displayContext) {
        var guiModel = SeparateLargeItemModels.modelFor(stack);
        if (guiModel != null && isGuiContext(displayContext)) {
            return ((FabricBakedModelManager) Minecraft.getInstance().getModelManager()).getModel(guiModel);
        }
        return original;
    }

    private static boolean isGuiContext(ItemDisplayContext displayContext) {
        return displayContext == ItemDisplayContext.GUI
                || displayContext == ItemDisplayContext.GROUND
                || displayContext == ItemDisplayContext.FIXED;
    }
}
