package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.client.renderer.PortalGunPortalViewRenderer;
import com.craisinlord.antarchy.content.client.renderer.PortalStencilTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererPortalLifecycleMixin {
    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void antarchy$preparePortalStencil(CallbackInfo ci) {
        if (PortalGunPortalViewRenderer.isEnabled()) {
            PortalStencilTarget.prepareMainTarget(Minecraft.getInstance().getMainRenderTarget());
        }
    }

    @Inject(method = "setLevel", at = @At("HEAD"))
    private void antarchy$clearPortalViewsOnLevelChange(ClientLevel level, CallbackInfo ci) {
        PortalGunPortalViewRenderer.releaseTargets();
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void antarchy$clearPortalViewsOnClose(CallbackInfo ci) {
        PortalGunPortalViewRenderer.releaseTargets();
        PortalStencilTarget.clear();
    }

    @Inject(method = "allChanged", at = @At("TAIL"))
    private void antarchy$clearPortalViewsOnRendererRebuild(CallbackInfo ci) {
        PortalGunPortalViewRenderer.releaseTargets();
    }
}
