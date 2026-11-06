package com.craisinlord.antarchy.fabric.mixin.client;

import com.craisinlord.antarchy.content.item.BrutalflyElytraItem;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Gui.class)
public abstract class BrutalflyElytraExperienceBarMixin {
    @Inject(method = "nextContextualInfoState", at = @At("RETURN"), cancellable = true)
    private void antarchy$hideExperienceBarForBrutalfly(CallbackInfoReturnable<Object> cir) {
        if (cir.getReturnValue() instanceof Enum<?> info && info.name().equals("EXPERIENCE") && antarchy$isBrutalflyFlying()) {
            cir.setReturnValue(antarchy$emptyInfo(info));
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object antarchy$emptyInfo(Enum<?> info) {
        return Enum.valueOf((Class) info.getDeclaringClass(), "EMPTY");
    }

    @WrapOperation(
            method = "extractHotbarAndDecorations",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/contextualbar/ContextualBarRenderer;extractExperienceLevel(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;I)V"
            )
    )
    private void antarchy$hideExperienceLevelForBrutalfly(GuiGraphicsExtractor graphics, Font font, int experienceLevel, Operation<Void> original) {
        if (!antarchy$isBrutalflyFlying()) {
            original.call(graphics, font, experienceLevel);
        }
    }

    private static boolean antarchy$isBrutalflyFlying() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player != null && BrutalflyElytraItem.isWearingBrutalflyElytra(player) && player.isFallFlying();
    }
}
