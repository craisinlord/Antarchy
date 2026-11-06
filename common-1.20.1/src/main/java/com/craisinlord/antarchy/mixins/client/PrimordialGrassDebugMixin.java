package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.client.renderer.PrimordialGrassRenderer;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DebugScreenOverlay.class)
public abstract class PrimordialGrassDebugMixin {
    @Inject(method = "getGameInformation", at = @At("RETURN"))
    private void antarchy$grassDebugLine(CallbackInfoReturnable<List<String>> callback) {
        PrimordialGrassRenderer.addDebugLines(callback.getReturnValue());
    }
}
