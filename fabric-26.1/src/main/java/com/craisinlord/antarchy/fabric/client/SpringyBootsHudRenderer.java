package com.craisinlord.antarchy.fabric.client;

import com.craisinlord.antarchy.content.item.SpringyBootsHelper;
import com.craisinlord.antarchy.content.item.SpringyBootsItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.ARGB;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public final class SpringyBootsHudRenderer {
    private static final Identifier JUMP_BAR_BACKGROUND = Identifier.withDefaultNamespace("textures/gui/sprites/hud/jump_bar_background.png");
    private static final Identifier JUMP_BAR_PROGRESS = Identifier.withDefaultNamespace("textures/gui/sprites/hud/jump_bar_progress.png");
    private static final int BAR_WIDTH = 182;
    private static final int BAR_HEIGHT = 5;

    private SpringyBootsHudRenderer() {
    }

    public static void render(GuiGraphicsExtractor guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.screen != null || player.isDeadOrDying()) {
            return;
        }
        if (!SpringyBootsItem.isWearingSpringyBoots(player)) {
            return;
        }
        if (!SpringyBootsClientHandler.isCharging()) {
            return;
        }

        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        int x0 = width / 2 - BAR_WIDTH / 2;
        int y0 = height - 29;

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, JUMP_BAR_BACKGROUND, x0, y0, 0, 0, BAR_WIDTH, BAR_HEIGHT, BAR_WIDTH, BAR_HEIGHT);

        int chargeTicks = SpringyBootsClientHandler.getChargeTicks();
        float chargeRatio = Mth.clamp(chargeTicks / (float) SpringyBootsHelper.CHARGE_TICKS_MAX, 0.0F, 1.0F);
        int filledWidth = Mth.clamp((int) (chargeRatio * BAR_WIDTH), 0, BAR_WIDTH);
        if (filledWidth > 0) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, JUMP_BAR_PROGRESS, x0, y0, 0, 0, filledWidth, BAR_HEIGHT, BAR_WIDTH, BAR_HEIGHT, ARGB.colorFromFloat(0.35F + chargeRatio * 0.65F, 1.0F, 1.0F, 1.0F));
        }
    }
}
