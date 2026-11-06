package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.client.ThrowbackClientState;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Gui.class)
public abstract class ThrowbackHungerBarMixin {
    @Redirect(
            method = "renderPlayerHealth",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V")
    )
    private void antarchy$fadeHungerSprites(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y, int u, int v, int width, int height) {
        boolean hungerSprite = v == 27 || v == 144;
        float hidden = AntarchySettings.throwbackClassicFood() && hungerSprite
                ? ThrowbackClientState.hungerHidden(Minecraft.getInstance().getFrameTime()) : 0.0F;
        if (hidden >= 0.99F) {
            return;
        }
        if (hidden > 0.01F) {
            RenderSystem.enableBlend();
            guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F - hidden);
        }
        guiGraphics.blit(texture, x, y, u, v, width, height);
        if (hidden > 0.01F) {
            guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
}
