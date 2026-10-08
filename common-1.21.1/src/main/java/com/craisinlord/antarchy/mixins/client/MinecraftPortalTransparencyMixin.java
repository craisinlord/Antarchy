package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.client.renderer.PortalGunPortalSceneRenderer;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftPortalTransparencyMixin {
    @Inject(method = "useShaderTransparency", at = @At("HEAD"), cancellable = true)
    private static void antarchy$disableShaderTransparencyInPortalScenes(CallbackInfoReturnable<Boolean> cir) {
        if (PortalGunPortalSceneRenderer.isRenderingScene()) {
            cir.setReturnValue(false);
        }
    }
}
