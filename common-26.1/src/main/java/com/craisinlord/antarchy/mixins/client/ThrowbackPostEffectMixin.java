package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.client.ThrowbackDreamRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(GameRenderer.class)
public abstract class ThrowbackPostEffectMixin {
    @Inject(method = "getRequestedPostEffects", at = @At("RETURN"))
    private void antarchy$syncThrowbackPostEffect(CallbackInfoReturnable<List<Identifier>> cir) {
        ThrowbackDreamRenderer.syncRequestedEffects(cir.getReturnValue());
    }

    @Inject(method = "applyPostEffects", at = @At("HEAD"))
    private void antarchy$updateThrowbackUniforms(CallbackInfo ci) {
        ThrowbackDreamRenderer.updateUniforms();
    }
}
