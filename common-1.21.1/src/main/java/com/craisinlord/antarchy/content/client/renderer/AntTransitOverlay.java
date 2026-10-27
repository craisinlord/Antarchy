package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.client.AntTransitClientState;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class AntTransitOverlay {
    private static final ResourceLocation VIGNETTE = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/misc/descent_vignette.png");
    private static final float RAINBOW_ALPHA = 0.28F;

    private AntTransitOverlay() {
    }

    public static void render(GuiGraphics graphics, float partialTick) {
        float vignette = AntTransitClientState.vignette(partialTick);
        float fill = AntTransitClientState.fill(partialTick);
        float prism = AntTransitClientState.prism(partialTick);
        if (vignette <= 0.003F && fill <= 0.003F && prism <= 0.003F) {
            return;
        }
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        if (prism > 0.003F) {
            float hue = AntTransitClientState.time(partialTick) * (0.01F + 0.04F * prism);
            int rgb = Mth.hsvToRgb(hue - (float) Math.floor(hue), 0.9F, 1.0F);
            graphics.fill(0, 0, width, height, Math.round(prism * RAINBOW_ALPHA * 255.0F) << 24 | rgb & 0xFFFFFF);
        }
        if (vignette > 0.003F) {
            float[] color = AntTransitClientState.vignetteColor();
            graphics.setColor(color[0], color[1], color[2], Math.min(1.0F, vignette));
            graphics.blit(VIGNETTE, 0, 0, width, height, 0.0F, 0.0F, 256, 256, 256, 256);
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
        if (fill > 0.003F) {
            float[] color = AntTransitClientState.fillColor();
            int rgb = Math.round(color[0] * 255.0F) << 16 | Math.round(color[1] * 255.0F) << 8 | Math.round(color[2] * 255.0F);
            graphics.fill(0, 0, width, height, Math.round(Math.min(1.0F, fill) * 255.0F) << 24 | rgb);
        }
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }
}
