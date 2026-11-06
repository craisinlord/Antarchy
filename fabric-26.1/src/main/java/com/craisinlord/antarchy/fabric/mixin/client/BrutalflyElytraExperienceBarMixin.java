package com.craisinlord.antarchy.fabric.mixin.client;

import com.craisinlord.antarchy.content.item.BrutalflyElytraItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.contextualbar.ContextualBarRenderer;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
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

    @Redirect(
            method = "extractHotbarAndDecorations",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/contextualbar/ContextualBarRenderer;extractExperienceLevel(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;I)V"
            )
    )
    private void antarchy$hideExperienceLevelForBrutalfly(GuiGraphicsExtractor graphics, Font font, int experienceLevel) {
        if (!antarchy$isBrutalflyFlying()) {
            ContextualBarRenderer.extractExperienceLevel(graphics, font, experienceLevel);
        }
    }

    private static boolean antarchy$isBrutalflyFlying() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player != null && BrutalflyElytraItem.isWearingBrutalflyElytra(player) && player.isFallFlying();
    }
}
