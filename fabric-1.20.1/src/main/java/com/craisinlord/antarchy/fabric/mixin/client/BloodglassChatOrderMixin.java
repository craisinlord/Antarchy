package com.craisinlord.antarchy.fabric.mixin.client;

import com.craisinlord.antarchy.fabric.client.BloodglassHudRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class BloodglassChatOrderMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void antarchy$renderBloodglassBeforeChat(GuiGraphics guiGraphics, float partialTick, CallbackInfo ci) {
        BloodglassHudRenderer.render(guiGraphics);
    }
}
