package com.craisinlord.antarchy.fabric.mixin.client;

import com.craisinlord.antarchy.fabric.client.SeparateLargeItemModels;
import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.fabric.api.client.model.loading.v1.FabricBakedModelManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(GuiGraphics.class)
public abstract class SeparateLargeItemGuiLightingMixin {
    @ModifyVariable(
            method = "renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;IIII)V",
            at = @At("STORE"),
            ordinal = 0
    )
    private BakedModel antarchy$useGuiModelForLighting(BakedModel original, @Local(argsOnly = true) ItemStack stack) {
        var guiModel = SeparateLargeItemModels.modelFor(stack);
        if (guiModel != null) {
            return ((FabricBakedModelManager) Minecraft.getInstance().getModelManager()).getModel(guiModel);
        }
        return original;
    }
}
