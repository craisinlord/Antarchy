package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.client.renderer.AntTransitOverlay;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class AntTransitGuiMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void antarchy$descentOverlay(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        AntTransitOverlay.render(graphics, deltaTracker.getGameTimeDeltaPartialTick(false));
    }
}
