package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.client.ThrowbackClientState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.contextualbar.ExperienceBar;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExperienceBar.class)
public abstract class ThrowbackExperienceBarMixin {
    @Inject(method = "extractBackground", at = @At("HEAD"), cancellable = true)
    private void antarchy$hideExperienceBarWhenFullyFaded(GuiGraphicsExtractor graphics, net.minecraft.client.DeltaTracker deltaTracker, CallbackInfo ci) {
        if (AntarchySettings.throwbackClassicFood() && antarchy$hiddenAmount() >= 0.99F) {
            ci.cancel();
        }
    }

    @Redirect(method = "extractBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V"))
    private void antarchy$fadeExperienceBackground(GuiGraphicsExtractor graphics, RenderPipeline pipeline, Identifier sprite,
                                                    int x, int y, int width, int height) {
        float alpha = AntarchySettings.throwbackClassicFood() ? 1.0F - antarchy$hiddenAmount() : 1.0F;
        if (alpha > 0.01F) {
            graphics.blitSprite(pipeline, sprite, x, y, width, height, alpha);
        }
    }

    @Redirect(method = "extractBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIIIIIII)V"))
    private void antarchy$fadeExperienceProgress(GuiGraphicsExtractor graphics, RenderPipeline pipeline, Identifier sprite,
                                                  int sourceWidth, int sourceHeight, int sourceX, int sourceY,
                                                  int x, int y, int width, int height) {
        float alpha = AntarchySettings.throwbackClassicFood() ? 1.0F - antarchy$hiddenAmount() : 1.0F;
        if (alpha > 0.01F) {
            graphics.blitSprite(pipeline, sprite, sourceWidth, sourceHeight, sourceX, sourceY, x, y, width, height, ARGB.white(alpha));
        }
    }

    private static float antarchy$hiddenAmount() {
        return ThrowbackClientState.hungerHidden(Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false));
    }
}
