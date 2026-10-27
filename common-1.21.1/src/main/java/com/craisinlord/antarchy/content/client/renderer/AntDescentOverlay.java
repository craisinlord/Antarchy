package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.client.AntDescentClientState;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public final class AntDescentOverlay {
    private static final ResourceLocation VIGNETTE = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/misc/descent_vignette.png");
    private static final int BLACK = 0x000000;
    private static final int WHITE = 0xFFF8EC;

    private AntDescentOverlay() {
    }

    public static void render(GuiGraphics graphics, float partialTick) {
        float vignette = AntDescentClientState.vignette(partialTick);
        float fill = AntDescentClientState.fill(partialTick);
        if (vignette <= 0.003F && fill <= 0.003F) {
            return;
        }
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        if (vignette > 0.003F) {
            graphics.setColor(1.0F, 1.0F, 1.0F, Math.min(1.0F, vignette));
            graphics.blit(VIGNETTE, 0, 0, width, height, 0.0F, 0.0F, 256, 256, 256, 256);
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
        if (fill > 0.003F) {
            int alpha = Math.round(Math.min(1.0F, fill) * 255.0F);
            graphics.fill(0, 0, width, height, alpha << 24 | (AntDescentClientState.home() ? WHITE : BLACK));
        }
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }
}
