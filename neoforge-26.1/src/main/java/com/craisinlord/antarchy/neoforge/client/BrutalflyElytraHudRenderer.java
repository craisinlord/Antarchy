package com.craisinlord.antarchy.neoforge.client;

import com.craisinlord.antarchy.content.item.BrutalflyElytraFlightHelper;
import com.craisinlord.antarchy.content.item.BrutalflyElytraItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.ARGB;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public final class BrutalflyElytraHudRenderer {
    private static final Identifier JUMP_BAR_BACKGROUND = Identifier.withDefaultNamespace("textures/gui/sprites/hud/jump_bar_background.png");
    private static final Identifier JUMP_BAR_PROGRESS = Identifier.withDefaultNamespace("textures/gui/sprites/hud/jump_bar_progress.png");
    private static final int BAR_WIDTH = 182;
    private static final int BAR_HEIGHT = 5;

    private BrutalflyElytraHudRenderer() {
    }

    public static void render(GuiGraphicsExtractor guiGraphics) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.screen != null || player.isDeadOrDying()) {
            return;
        }
        if (!BrutalflyElytraItem.isWearingBrutalflyElytra(player) || !player.isFallFlying()) {
            return;
        }

        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();
        int x0 = width / 2 - BAR_WIDTH / 2;
        int y0 = height - 29;

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, JUMP_BAR_BACKGROUND, x0, y0, 0, 0, BAR_WIDTH, BAR_HEIGHT, BAR_WIDTH, BAR_HEIGHT);

        int chargeTicks = BrutalflyElytraClientHandler.getChargeTicks();
        float chargeRatio = Mth.clamp(chargeTicks / (float) BrutalflyElytraFlightHelper.FLAP_CHARGE_TICKS_MAX, 0.0F, 1.0F);
        int filledWidth = Mth.clamp((int) (chargeRatio * BAR_WIDTH), 0, BAR_WIDTH);
        if (filledWidth > 0) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, JUMP_BAR_PROGRESS, x0, y0, 0, 0, filledWidth, BAR_HEIGHT, BAR_WIDTH, BAR_HEIGHT, ARGB.colorFromFloat(0.35F + chargeRatio * 0.65F, 1.0F, 1.0F, 1.0F));
        }
    }
}
