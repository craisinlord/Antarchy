package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.client.renderer.PrimordialGrassRenderer;
import com.mojang.renderpearl.api.commands.RenderPass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class PrimordialGrassRenderMixin {
    @Inject(method = "addMainPass", at = @At("HEAD"))
    private void antarchy$prepareGrass(CallbackInfo callback) {
        Minecraft minecraft = Minecraft.getInstance();
        PrimordialGrassRenderer.render(minecraft.gameRenderer.mainCamera(), minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false));
    }

    @Inject(method = "executeSolid", at = @At("RETURN"))
    private void antarchy$drawGrass(ChunkSectionsToRender chunkSectionsToRender, FeatureRenderDispatcher.PreparedFrame featureFrame, RenderPass renderPass, CallbackInfo callback) {
        PrimordialGrassRenderer.draw(renderPass);
    }
}
