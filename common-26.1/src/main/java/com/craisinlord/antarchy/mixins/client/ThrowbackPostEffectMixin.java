package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.client.ThrowbackDreamRenderer;
import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class ThrowbackPostEffectMixin {
    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private CrossFrameResourcePool resourcePool;

    @Inject(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/GameRenderer;postEffectId:Lnet/minecraft/resources/Identifier;", ordinal = 0))
    private void antarchy$applyThrowbackPostEffect(DeltaTracker deltaTracker, boolean advanceGameTime, CallbackInfo ci) {
        ThrowbackDreamRenderer.process(this.minecraft.getMainRenderTarget(), this.resourcePool);
    }
}
