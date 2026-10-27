package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.entity.ElythiaFrog;
import net.minecraft.client.renderer.entity.FrogRenderer;
import net.minecraft.client.renderer.entity.state.FrogRenderState;
import net.minecraft.world.entity.animal.frog.Frog;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FrogRenderer.class)
public abstract class FrogRendererElythiaMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/animal/frog/Frog;Lnet/minecraft/client/renderer/entity/state/FrogRenderState;F)V", at = @At("TAIL"))
    private void antarchy$useElythiaTexture(Frog frog, FrogRenderState state, float partialTicks, CallbackInfo ci) {
        if (!ElythiaFrog.isElythiaFrog(frog)) {
            return;
        }

        state.texture = ElythiaFrog.TEXTURE;
    }
}
