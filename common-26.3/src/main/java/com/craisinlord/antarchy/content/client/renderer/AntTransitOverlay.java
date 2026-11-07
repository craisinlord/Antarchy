package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.AntTransitClientState;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public final class AntTransitOverlay {
    private static final float RAINBOW_ALPHA = 0.28F;
    private static final Identifier VIGNETTE = Identifier.fromNamespaceAndPath("antarchy", "textures/misc/descent_vignette.png");

    private AntTransitOverlay() {
    }

    public static void render(GuiGraphicsExtractor graphics, float partialTick) {
        float vignette = AntTransitClientState.vignette(partialTick);
        float fill = AntTransitClientState.fill(partialTick);
        float prism = AntTransitClientState.prism(partialTick);
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        if (prism > 0.003F) {
            float hue = AntTransitClientState.time(partialTick) * (0.01F + 0.04F * prism);
            int rgb = Mth.hsvToRgb(hue - (float) Math.floor(hue), 0.9F, 1.0F);
            graphics.fill(0, 0, width, height, Math.round(prism * RAINBOW_ALPHA * 255.0F) << 24 | rgb & 0xFFFFFF);
        }
        if (vignette > 0.003F) {
            float[] color = AntTransitClientState.vignetteColor();
            int rgb = Math.round(color[0] * 255.0F) << 16 | Math.round(color[1] * 255.0F) << 8 | Math.round(color[2] * 255.0F);
            int alpha = Math.round(Math.min(1.0F, vignette) * 255.0F);
            graphics.blit(RenderPipelines.GUI_TEXTURED, VIGNETTE, 0, 0, 0.0F, 0.0F, width, height, 256, 256,
                    alpha << 24 | rgb);
        }
        if (fill > 0.003F) {
            float[] color = AntTransitClientState.fillColor();
            int rgb = Math.round(color[0] * 255.0F) << 16 | Math.round(color[1] * 255.0F) << 8 | Math.round(color[2] * 255.0F);
            graphics.fill(0, 0, width, height, Math.round(Math.min(1.0F, fill) * 255.0F) << 24 | rgb);
        }
    }
}
