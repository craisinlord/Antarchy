package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.client.renderer.PrimordialGrassRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.extract.LevelExtractor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelExtractor.class)
public abstract class PrimordialGrassVisibilityMixin {
    @Shadow
    @Final
    private LevelRenderer levelRenderer;

    @Inject(method = "applyFrustum", at = @At("RETURN"))
    private void antarchy$grassVisibilityChanged(Frustum frustum, CallbackInfo callback) {
        PrimordialGrassRenderer.visibilityChanged(this.levelRenderer.visibleSections());
    }
}
