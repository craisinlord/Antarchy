package com.craisinlord.antarchy.forge.mixins.client;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.client.ThrowbackClientState;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ForgeGui.class, remap = false)
public abstract class ThrowbackHungerBarMixin {
    @Unique
    private boolean antarchy$hungerFaded;

    @Inject(method = "renderFood", at = @At("HEAD"), cancellable = true)
    private void antarchy$fadeHungerInThrowbackZone(int width, int height, GuiGraphics guiGraphics, CallbackInfo ci) {
        this.antarchy$hungerFaded = false;
        if (!AntarchySettings.throwbackClassicFood()) {
            return;
        }
        float hidden = ThrowbackClientState.hungerHidden(Minecraft.getInstance().getFrameTime());
        if (hidden <= 0.01F) {
            return;
        }
        if (hidden >= 0.99F) {
            ((ForgeGui) (Object) this).rightHeight += 10;
            ci.cancel();
            return;
        }
        RenderSystem.enableBlend();
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F - hidden);
        this.antarchy$hungerFaded = true;
    }

    @Inject(method = "renderFood", at = @At("RETURN"))
    private void antarchy$restoreHungerColor(int width, int height, GuiGraphics guiGraphics, CallbackInfo ci) {
        if (this.antarchy$hungerFaded) {
            guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            this.antarchy$hungerFaded = false;
        }
    }
}
