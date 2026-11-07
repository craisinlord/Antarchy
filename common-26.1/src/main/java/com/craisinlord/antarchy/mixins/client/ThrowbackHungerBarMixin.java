package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.client.ThrowbackClientState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class ThrowbackHungerBarMixin {
    @Inject(method = "extractFood", at = @At("HEAD"), cancellable = true)
    private void antarchy$hideHungerWhenFullyFaded(GuiGraphicsExtractor graphics, Player player, int x, int y, CallbackInfo ci) {
        if (AntarchySettings.throwbackClassicFood() && antarchy$hiddenAmount() >= 0.99F) {
            ci.cancel();
        }
    }

    @Redirect(method = "extractFood", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V"))
    private void antarchy$fadeHungerSprites(GuiGraphicsExtractor graphics, RenderPipeline pipeline, Identifier sprite,
                                             int x, int y, int width, int height) {
        float hidden = AntarchySettings.throwbackClassicFood() ? antarchy$hiddenAmount() : 0.0F;
        if (hidden >= 0.99F) {
            return;
        }
        graphics.blitSprite(pipeline, sprite, x, y, width, height, 1.0F - hidden);
    }

    private static float antarchy$hiddenAmount() {
        return ThrowbackClientState.hungerHidden(Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false));
    }
}
