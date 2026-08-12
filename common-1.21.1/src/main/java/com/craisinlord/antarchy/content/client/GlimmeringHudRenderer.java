package com.craisinlord.antarchy.content.client;

import com.craisinlord.antarchy.content.AntarchyObjects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;

public final class GlimmeringHudRenderer {
    private static final int MAX_ALPHA = 0x40;
    private static final int TINT_RGB = 0x66CCFF;
    private static final float FADE_SPEED = 0.05F;

    private static float fadeProgress = 0.0F;

    private GlimmeringHudRenderer() {
    }

    public static void render(GuiGraphics guiGraphics) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        Holder<MobEffect> glimmering = AntarchyObjects.GLIMMERING_EFFECT.get();
        boolean active = player != null && !player.isDeadOrDying() && glimmering != null && player.hasEffect(glimmering);

        fadeProgress = active
                ? Math.min(1.0F, fadeProgress + FADE_SPEED)
                : Math.max(0.0F, fadeProgress - FADE_SPEED);
        if (fadeProgress <= 0.0F) {
            return;
        }

        int alpha = Mth.floor(MAX_ALPHA * fadeProgress);
        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();
        guiGraphics.fill(0, 0, width, height, (alpha << 24) | TINT_RGB);
    }
}
