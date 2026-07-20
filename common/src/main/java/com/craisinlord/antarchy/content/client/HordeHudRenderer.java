package com.craisinlord.antarchy.content.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

public final class HordeHudRenderer {
    private HordeHudRenderer() {
    }

    public static void render(GuiGraphics guiGraphics) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.player.isDeadOrDying()) {
            return;
        }

        float intensity = HordeClientState.intensity();
        if (intensity <= 0.01F) {
            return;
        }

        float pulse = 0.9F + 0.1F * Mth.sin(minecraft.player.tickCount * ((float) Math.PI * 2.0F / 90.0F));
        int alpha = Mth.clamp((int) (intensity * pulse * 0x78), 0, 0x78);
        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();
        guiGraphics.fill(0, 0, width, height, (alpha << 24) | 0xD84A16);
    }
}
