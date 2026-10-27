package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.client.ThrowbackClientState;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class ThrowbackHungerBarMixin {
    @Unique
    private boolean antarchy$hungerFaded;

    @Inject(method = "renderFood", at = @At("HEAD"), cancellable = true)
    private void antarchy$fadeHungerInThrowbackZone(GuiGraphics guiGraphics, Player player, int y, int x, CallbackInfo ci) {
        this.antarchy$hungerFaded = false;
        if (!AntarchySettings.throwbackClassicFood()) {
            return;
        }
        float hidden = ThrowbackClientState.hungerHidden(Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false));
        if (hidden <= 0.01F) {
            return;
        }
        if (hidden >= 0.99F) {
            ci.cancel();
            return;
        }
        RenderSystem.enableBlend();
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F - hidden);
        this.antarchy$hungerFaded = true;
    }

    @Inject(method = "renderFood", at = @At("RETURN"))
    private void antarchy$restoreHungerColor(GuiGraphics guiGraphics, Player player, int y, int x, CallbackInfo ci) {
        if (this.antarchy$hungerFaded) {
            guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            this.antarchy$hungerFaded = false;
        }
    }
}
