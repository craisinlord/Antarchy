package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.client.ThrowbackClientState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.contextualbar.ContextualBar;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Hud.class)
public abstract class ThrowbackExperienceLevelMixin {
    @Unique
    private static final int MIN_TEXT_ALPHA = 4;

    @Redirect(
            method = {"extractHotbarAndDecorations", "extractExperienceLevel"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBar;extractExperienceLevel(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;I)V")
    )
    private void antarchy$fadeExperienceLevel(GuiGraphicsExtractor graphics, Font font, int experienceLevel) {
        float hidden = AntarchySettings.throwbackClassicFood()
                ? ThrowbackClientState.hungerHidden(Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false))
                : 0.0F;
        if (hidden <= 0.01F) {
            ContextualBar.extractExperienceLevel(graphics, font, experienceLevel);
            return;
        }
        if (hidden >= 0.99F) {
            return;
        }
        int alpha = Math.max(MIN_TEXT_ALPHA, Mth.floor((1.0F - hidden) * 255.0F)) << 24;
        Component text = Component.translatable("gui.experience.level", experienceLevel);
        int x = (graphics.guiWidth() - font.width(text)) / 2;
        int y = graphics.guiHeight() - 24 - 9 - 2;
        graphics.text(font, text, x + 1, y, alpha, false);
        graphics.text(font, text, x - 1, y, alpha, false);
        graphics.text(font, text, x, y + 1, alpha, false);
        graphics.text(font, text, x, y - 1, alpha, false);
        graphics.text(font, text, x, y, alpha | 0x80FF20, false);
    }
}
