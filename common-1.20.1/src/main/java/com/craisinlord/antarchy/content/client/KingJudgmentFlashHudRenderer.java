package com.craisinlord.antarchy.content.client;

import net.minecraft.client.gui.GuiGraphics;

public final class KingJudgmentFlashHudRenderer {
    private KingJudgmentFlashHudRenderer() {
    }

    public static void render(GuiGraphics guiGraphics) {
        int alpha = Math.round(KingJudgmentFlashClientState.alpha() * 255.0F);
        if (alpha <= 0) {
            return;
        }
        guiGraphics.fill(0, 0, guiGraphics.guiWidth(), guiGraphics.guiHeight(),
                alpha << 24 | 0x00FFFFFF);
    }
}
