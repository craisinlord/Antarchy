package com.craisinlord.antarchy.fabric.mixin.client;

import com.craisinlord.antarchy.content.item.BrutalflyElytraItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class BrutalflyElytraExperienceBarMixin {
    @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
    private void antarchy$hideExperienceBarForBrutalfly(GuiGraphics guiGraphics, int x, CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && BrutalflyElytraItem.isWearingBrutalflyElytra(player) && player.isFallFlying()) {
            ci.cancel();
        }
    }
}
