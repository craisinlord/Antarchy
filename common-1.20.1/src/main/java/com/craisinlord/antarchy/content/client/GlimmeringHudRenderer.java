package com.craisinlord.antarchy.content.client;

import com.craisinlord.antarchy.content.AntarchyObjects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;

public final class GlimmeringHudRenderer {
    private static final int TINT_ALPHA = 0x40;
    private static final int TINT_RGB = 0x66CCFF;

    private GlimmeringHudRenderer() {
    }

    public static void render(GuiGraphics guiGraphics) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        MobEffect glimmering = AntarchyObjects.GLIMMERING_EFFECT.get();
        if (player == null || player.isDeadOrDying() || glimmering == null || !player.hasEffect(glimmering)) {
            return;
        }

        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();
        guiGraphics.fill(0, 0, width, height, (TINT_ALPHA << 24) | TINT_RGB);
    }
}
