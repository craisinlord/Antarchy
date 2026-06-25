package com.craisinlord.antarchy.fabric.client;

import com.craisinlord.antarchy.content.item.JumpyBootsHelper;
import com.craisinlord.antarchy.content.item.JumpyBootsItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public final class JumpyBootsHudRenderer {
    private static final ResourceLocation JUMP_BAR_BACKGROUND = ResourceLocation.withDefaultNamespace("textures/gui/sprites/hud/jump_bar_background.png");
    private static final ResourceLocation JUMP_BAR_PROGRESS = ResourceLocation.withDefaultNamespace("textures/gui/sprites/hud/jump_bar_progress.png");
    private static final int BAR_WIDTH = 182;
    private static final int BAR_HEIGHT = 5;

    private JumpyBootsHudRenderer() {
    }

    public static void render(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.screen != null || player.isDeadOrDying()) {
            return;
        }
        if (!JumpyBootsItem.isWearingJumpyBoots(player)) {
            return;
        }
        if (!JumpyBootsClientHandler.isCharging()) {
            return;
        }

        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        int x0 = width / 2 - BAR_WIDTH / 2;
        int y0 = height - 29;

        guiGraphics.blit(JUMP_BAR_BACKGROUND, x0, y0, 0, 0, BAR_WIDTH, BAR_HEIGHT, BAR_WIDTH, BAR_HEIGHT);

        int chargeTicks = JumpyBootsClientHandler.getChargeTicks();
        float chargeRatio = Mth.clamp(chargeTicks / (float) JumpyBootsHelper.CHARGE_TICKS_MAX, 0.0F, 1.0F);
        int filledWidth = Mth.clamp((int) (chargeRatio * BAR_WIDTH), 0, BAR_WIDTH);
        if (filledWidth > 0) {
            guiGraphics.setColor(1.0F, 1.0F, 1.0F, 0.35F + chargeRatio * 0.65F);
            guiGraphics.blit(JUMP_BAR_PROGRESS, x0, y0, 0, 0, filledWidth, BAR_HEIGHT, BAR_WIDTH, BAR_HEIGHT);
            guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
}
