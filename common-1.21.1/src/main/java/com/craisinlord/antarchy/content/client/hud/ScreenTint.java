package com.craisinlord.antarchy.content.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;

public final class ScreenTint {
    private ScreenTint() {
    }

    public static void fill(GuiGraphics guiGraphics, int x0, int y0, int x1, int y1, int color) {
        guiGraphics.flush();
        RenderSystem.depthMask(false);
        guiGraphics.fill(x0, y0, x1, y1, color);
        guiGraphics.flush();
        RenderSystem.depthMask(true);
    }
}
