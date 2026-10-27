package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.client.ThrowbackClientState;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class ThrowbackExperienceBarMixin {
    @Unique
    private static final int MIN_TEXT_ALPHA = 4;

    @Unique
    private boolean antarchy$experienceFaded;

    @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
    private void antarchy$fadeExperienceBar(GuiGraphics guiGraphics, int x, CallbackInfo ci) {
        this.antarchy$experienceFaded = false;
        float hidden = antarchy$hiddenAmount();
        if (hidden <= 0.01F) {
            return;
        }
        if (hidden >= 0.99F) {
            ci.cancel();
            return;
        }
        RenderSystem.enableBlend();
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F - hidden);
        this.antarchy$experienceFaded = true;
    }

    @Inject(method = "renderExperienceBar", at = @At("RETURN"))
    private void antarchy$restoreExperienceBarColor(GuiGraphics guiGraphics, int x, CallbackInfo ci) {
        if (this.antarchy$experienceFaded) {
            guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            this.antarchy$experienceFaded = false;
        }
    }

    @Inject(method = "renderExperienceLevel", at = @At("HEAD"), cancellable = true)
    private void antarchy$hideExperienceLevel(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (antarchy$hiddenAmount() >= 0.99F) {
            ci.cancel();
        }
    }

    @ModifyArg(
            method = "renderExperienceLevel",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I"),
            index = 4
    )
    private int antarchy$fadeExperienceLevel(int color) {
        float hidden = antarchy$hiddenAmount();
        if (hidden <= 0.01F) {
            return color;
        }
        int alpha = Math.max(MIN_TEXT_ALPHA, Mth.floor((1.0F - hidden) * 255.0F));
        return (alpha << 24) | (color & 0x00FFFFFF);
    }

    @Unique
    private static float antarchy$hiddenAmount() {
        if (!AntarchySettings.throwbackClassicFood()) {
            return 0.0F;
        }
        return ThrowbackClientState.hungerHidden(Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false));
    }
}
