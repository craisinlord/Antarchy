package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.client.renderer.PrimordialGrassRenderer;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugScreenOverlay.class)
public abstract class PrimordialGrassDebugMixin {
    @Inject(method = "extractLines", at = @At("HEAD"))
    private void antarchy$grassDebugLine(GuiGraphicsExtractor graphics, List<String> lines, boolean alignLeft, int scaledScreenWidth, CallbackInfo callback) {
        if (alignLeft && Minecraft.getInstance().debugEntries.isOverlayVisible()) PrimordialGrassRenderer.addDebugLines(lines);
    }
}
