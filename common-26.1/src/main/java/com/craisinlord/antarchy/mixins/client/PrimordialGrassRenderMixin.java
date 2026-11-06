package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.client.renderer.PrimordialGrassRenderer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class PrimordialGrassRenderMixin {
    @Shadow
    @Final
    private ObjectArrayList<SectionRenderDispatcher.RenderSection> visibleSections;

    @Inject(method = "applyFrustum", at = @At("RETURN"))
    private void antarchy$grassVisibilityChanged(Frustum frustum, CallbackInfo callback) {
        PrimordialGrassRenderer.visibilityChanged(this.visibleSections);
    }

    @Inject(
            method = "/^lambda\\$addMainPass\\$/",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher;renderSolidFeatures()V", shift = At.Shift.AFTER)
    )
    private void antarchy$renderGrass(CallbackInfo callback) {
        Minecraft minecraft = Minecraft.getInstance();
        PrimordialGrassRenderer.render(minecraft.gameRenderer.getMainCamera(), minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false));
    }
}
