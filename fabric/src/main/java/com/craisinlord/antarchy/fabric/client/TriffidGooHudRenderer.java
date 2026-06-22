package com.craisinlord.antarchy.fabric.client;

import com.craisinlord.antarchy.fabric.AntarchyFabricContent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;

public final class TriffidGooHudRenderer {
    private TriffidGooHudRenderer() {
    }

    private static float fadeProgress = 0f;
    private static final float FADE_SPEED = 0.05f;
    private static final int MAX_ALPHA = 0x55;

    public static void render(GuiGraphics guiGraphics) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || player.isDeadOrDying()) {
            fadeProgress = Math.max(0f, fadeProgress - FADE_SPEED);
            if (fadeProgress <= 0f) return;
        } else {
            Block goo = AntarchyFabricContent.TRIFFID_GOO_BLOCK.get();
            BlockPos feet = player.blockPosition();
            boolean inGoo = player.level().getBlockState(feet).is(goo)
                         || player.level().getBlockState(feet.above()).is(goo);

            if (inGoo) {
                fadeProgress = Math.min(1f, fadeProgress + FADE_SPEED);
            } else {
                fadeProgress = Math.max(0f, fadeProgress - FADE_SPEED);
            }

            if (fadeProgress <= 0f || minecraft.screen != null) return;
        }

        int alpha = Mth.floor(MAX_ALPHA * fadeProgress);
        int color = (alpha << 24) | 0x003300;
        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();
        guiGraphics.fill(0, 0, width, height, color);
    }
}
